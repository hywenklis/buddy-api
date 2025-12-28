#!/bin/bash

# Script to update and merge Dependabot branches
# This script:
# 1. Fetches all branches
# 2. Updates each Dependabot branch with the latest develop
# 3. Pushes the updated branch
# 4. Waits for GitHub Actions to complete
# 5. Merges the PR if actions pass

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if gh CLI is installed
check_gh_cli() {
    if ! command -v gh &> /dev/null; then
        print_error "GitHub CLI (gh) is not installed. Please install it first."
        print_info "Visit: https://cli.github.com/"
        exit 1
    fi
}

# Function to check if gh is authenticated
check_gh_auth() {
    if ! gh auth status &> /dev/null; then
        print_error "GitHub CLI is not authenticated. Please run: gh auth login"
        exit 1
    fi
}

# Function to get all dependabot branches
get_dependabot_branches() {
    git fetch --all --prune
    git branch -r | grep "origin/dependabot" | sed 's/origin\///' | tr -d ' '
}

# Function to update branch with develop
update_branch_with_develop() {
    local branch=$1
    print_info "Updating branch: $branch with develop"
    
    # Checkout the branch
    git checkout "$branch" || {
        print_warning "Branch $branch doesn't exist locally, creating from remote"
        git checkout -b "$branch" "origin/$branch"
    }
    
    # Merge develop into the branch
    if git merge origin/develop --no-edit; then
        print_info "Successfully merged develop into $branch"
        
        # Push the updated branch
        git push origin "$branch"
        print_info "Pushed updated branch $branch"
        return 0
    else
        print_error "Merge conflict detected in $branch"
        git merge --abort
        return 1
    fi
}

# Function to get PR number for a branch
get_pr_number() {
    local branch=$1
    gh pr list --head "$branch" --json number --jq '.[0].number'
}

# Function to check if PR checks have passed
check_pr_status() {
    local pr_number=$1
    local status=$(gh pr view "$pr_number" --json statusCheckRollup --jq '.statusCheckRollup[].conclusion')
    
    # Check if all checks have passed
    if echo "$status" | grep -q "FAILURE\|CANCELLED\|TIMED_OUT"; then
        return 1
    elif echo "$status" | grep -q "PENDING\|IN_PROGRESS"; then
        return 2
    else
        return 0
    fi
}

# Function to wait for PR checks to complete
wait_for_checks() {
    local pr_number=$1
    local max_wait=1800  # 30 minutes
    local wait_time=0
    local check_interval=30
    
    print_info "Waiting for checks to complete for PR #$pr_number"
    
    while [ $wait_time -lt $max_wait ]; do
        check_pr_status "$pr_number"
        local status=$?
        
        if [ $status -eq 0 ]; then
            print_info "All checks passed for PR #$pr_number"
            return 0
        elif [ $status -eq 1 ]; then
            print_error "Some checks failed for PR #$pr_number"
            return 1
        else
            echo -n "."
            sleep $check_interval
            wait_time=$((wait_time + check_interval))
        fi
    done
    
    print_warning "Timeout waiting for checks to complete for PR #$pr_number"
    return 2
}

# Function to merge PR
merge_pr() {
    local pr_number=$1
    print_info "Merging PR #$pr_number"
    
    if gh pr merge "$pr_number" --squash --auto; then
        print_info "Successfully merged PR #$pr_number"
        return 0
    else
        print_error "Failed to merge PR #$pr_number"
        return 1
    fi
}

# Main execution
main() {
    print_info "Starting Dependabot branch update and merge process"
    
    # Check prerequisites
    check_gh_cli
    check_gh_auth
    
    # Ensure we're in the repository root
    if [ ! -d ".git" ]; then
        print_error "Not in a git repository"
        exit 1
    fi
    
    # Get current branch to return to later
    current_branch=$(git branch --show-current)
    
    # Fetch latest changes
    print_info "Fetching latest changes"
    git fetch --all --prune
    
    # Get all dependabot branches
    dependabot_branches=$(get_dependabot_branches)
    
    if [ -z "$dependabot_branches" ]; then
        print_info "No Dependabot branches found"
        exit 0
    fi
    
    print_info "Found Dependabot branches:"
    echo "$dependabot_branches"
    echo ""
    
    # Process each branch
    for branch in $dependabot_branches; do
        print_info "Processing branch: $branch"
        
        # Update branch with develop
        if ! update_branch_with_develop "$branch"; then
            print_warning "Skipping $branch due to merge conflict"
            continue
        fi
        
        # Get PR number
        pr_number=$(get_pr_number "$branch")
        
        if [ -z "$pr_number" ]; then
            print_warning "No PR found for branch $branch"
            continue
        fi
        
        print_info "Found PR #$pr_number for branch $branch"
        
        # Wait for checks to complete
        if wait_for_checks "$pr_number"; then
            # Merge the PR
            merge_pr "$pr_number"
        else
            print_warning "Skipping merge for PR #$pr_number due to failed checks"
        fi
        
        echo ""
    done
    
    # Return to original branch
    print_info "Returning to original branch: $current_branch"
    git checkout "$current_branch"
    
    print_info "Process completed"
}

# Run main function
main
