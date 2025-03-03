package com.buddy.api.domains.authentication.services.impls;

import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.services.FindProfile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final FindAccount findAccount;
    private final FindProfile findProfile;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        log.debug("Loading user details for email: {}", username);
        AccountDto account = findAccount.findByEmail(username);
        List<ProfileDto> profiles = findProfile.findByAccountEmail(account.email().value());

        String[] authorities = profiles.stream()
            .filter(profile -> !profile.isDeleted())
            .map(profile -> profile.profileType().name())
            .toArray(String[]::new);

        return User
            .withUsername(account.email().value())
            .password(account.password())
            .authorities(authorities)
            .disabled(account.isBlocked() || account.isDeleted())
            .build();
    }
}
