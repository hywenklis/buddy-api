package com.buddy.api.domains.authentication.dtos;

import com.buddy.api.domains.account.dtos.AccountDto;
import java.io.Serial;
import java.util.Collection;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class AuthenticatedUser implements UserDetails {

    @Serial
    private static final long serialVersionUID = -6582441640280677406L;

    private final String email;
    private final String password;
    private final boolean verified;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthenticatedUser(final AccountDto account,
                             final Collection<? extends GrantedAuthority> authorities) {
        this.email = account.email().value();
        this.password = account.password();
        this.verified = account.isVerified();
        this.active = !account.isBlocked() && !account.isDeleted();
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.verified;
    }
}
