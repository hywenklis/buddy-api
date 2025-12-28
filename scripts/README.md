# Dependabot Branch Management

This directory contains scripts to help manage Dependabot PRs in the repository.

## Prerequisites

- Git installed and configured
- [GitHub CLI (gh)](https://cli.github.com/) installed and authenticated
- Write access to the repository

## Installation

### GitHub CLI

If you don't have GitHub CLI installed:

**macOS:**
```bash
brew install gh
```

**Linux:**
```bash
# Debian/Ubuntu
sudo apt install gh

# Fedora
sudo dnf install gh

# Arch
sudo pacman -S github-cli
```

**Windows:**
```bash
winget install GitHub.cli
# or
choco install gh
```

### Authentication

After installing, authenticate with GitHub:
```bash
gh auth login
```

## Usage

### Automatic Mode

Run the script to automatically update all Dependabot branches with develop, wait for checks, and merge:

```bash
./scripts/update-dependabot-branches.sh
```

The script will:
1. Fetch all branches from the repository
2. Find all Dependabot branches (branches starting with `dependabot`)
3. For each Dependabot branch:
   - Merge the latest `develop` branch into it
   - Push the updated branch
   - Wait for GitHub Actions checks to complete
   - Merge the PR if all checks pass

### Manual Steps

If you prefer to do this manually or need to handle specific cases:

1. **Fetch the latest changes:**
   ```bash
   git fetch --all
   ```

2. **Update a specific Dependabot branch:**
   ```bash
   # Checkout the dependabot branch
   git checkout dependabot-gradle-develop-com.github.spotbugs-6.4.8
   
   # Merge develop into it
   git merge origin/develop
   
   # Resolve any conflicts if necessary
   
   # Push the updated branch
   git push origin dependabot-gradle-develop-com.github.spotbugs-6.4.8
   ```

3. **Check PR status:**
   ```bash
   # List all open PRs
   gh pr list
   
   # View specific PR details
   gh pr view <PR_NUMBER>
   
   # Check PR checks status
   gh pr checks <PR_NUMBER>
   ```

4. **Merge the PR after checks pass:**
   ```bash
   # Merge using squash
   gh pr merge <PR_NUMBER> --squash
   
   # Or use auto-merge
   gh pr merge <PR_NUMBER> --squash --auto
   ```

## Current Dependabot Branches

As of the last check, the following Dependabot branches are pending:

- `dependabot-github_actions-develop-actions-upload-artifact-6` - Update actions/upload-artifact to v6
- `dependabot-gradle-develop-com.github.spotbugs-6.4.8` - Update com.github.spotbugs to 6.4.8
- `dependabot-gradle-develop-com.github.spotbugs-6.0.23` - Update com.github.spotbugs to 6.0.23
- `dependabot-gradle-develop-com.palantir.git-version-4.2.0` - Update com.palantir.git-version to 4.2.0
- `dependabot-gradle-develop-org.springframework.boot-4.0.1` - Update org.springframework.boot to 4.0.1

## Troubleshooting

### Merge Conflicts

If you encounter merge conflicts:

1. The script will skip branches with conflicts
2. You can manually resolve them:
   ```bash
   git checkout <branch-name>
   git merge origin/develop
   # Resolve conflicts in your editor
   git add .
   git commit
   git push origin <branch-name>
   ```

### Failed Checks

If CI/CD checks fail:

1. Review the check details:
   ```bash
   gh pr checks <PR_NUMBER>
   ```
2. Click the link to view detailed logs in GitHub Actions
3. Fix the issues in the Dependabot branch if needed
4. Re-run the checks or wait for them to complete

### Authentication Issues

If you get authentication errors:

```bash
gh auth refresh
# or
gh auth login
```

## Notes

- The script uses squash merging by default
- It waits up to 30 minutes for checks to complete
- Failed or conflicted branches are skipped automatically
- The script returns you to your original branch when done

## Safety Features

- Aborts merge if conflicts are detected
- Skips PRs with failing checks
- Validates GitHub CLI installation and authentication before starting
- Provides colored output for easy monitoring
