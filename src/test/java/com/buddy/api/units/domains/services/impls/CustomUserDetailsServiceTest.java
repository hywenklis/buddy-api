package com.buddy.api.units.domains.services.impls;

import static com.buddy.api.builders.account.AccountBuilder.validAccountDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.buddy.api.builders.profile.ProfileBuilder;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.authentication.services.impls.CustomUserDetailsService;
import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import com.buddy.api.domains.profile.services.FindProfile;
import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.units.UnitTestAbstract;
import com.buddy.api.utils.RandomEmailUtils;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

class CustomUserDetailsServiceTest extends UnitTestAbstract {

    @Mock
    private FindAccount findAccount;

    @Mock
    private FindProfile findProfile;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Should load user details with ROLE_USER, SCOPE_VERIFIED and Profile Role")
    void should_load_user_details_successfully() {
        AccountDto accountDto = validAccountDto()
            .email(new EmailAddress(RandomEmailUtils.generateValidEmail()))
            .isBlocked(false)
            .isDeleted(false)
            .isVerified(true)
            .build();

        ProfileDto activeProfile = ProfileBuilder.profileDto()
            .profileType(ProfileTypeEnum.SHELTER)
            .isDeleted(false)
            .build();

        when(findAccount.findAccountForAuthentication(accountDto.email().value()))
            .thenReturn(accountDto);
        when(findProfile.findByAccountEmail(accountDto.email().value()))
            .thenReturn(List.of(activeProfile));

        UserDetails result = customUserDetailsService.loadUserByUsername(
            accountDto.email().value()
        );

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(accountDto.email().value());
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isAccountNonLocked()).isTrue();

        List<String> authorities = result.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        assertThat(authorities).hasSize(3)
            .contains("ROLE_USER")
            .contains("SCOPE_VERIFIED")
            .contains("ROLE_SHELTER");
    }

    @Test
    @DisplayName("Should return user with only ROLE_USER when unverified and no active profiles")
    void should_return_only_base_role_when_unverified_and_profiles_deleted() {
        AccountDto accountDto = validAccountDto()
            .email(new EmailAddress(RandomEmailUtils.generateValidEmail()))
            .isVerified(false)
            .isDeleted(false)
            .isBlocked(false)
            .build();

        ProfileDto deletedProfile = ProfileBuilder.profileDto().isDeleted(true).build();

        when(findAccount.findAccountForAuthentication(accountDto.email().value()))
            .thenReturn(accountDto);
        when(findProfile.findByAccountEmail(accountDto.email().value()))
            .thenReturn(List.of(deletedProfile));

        UserDetails result = customUserDetailsService.loadUserByUsername(
            accountDto.email().value()
        );

        assertThat(result).isNotNull();
        assertThat(result.isEnabled()).isTrue();

        assertThat(result.getAuthorities())
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Should return LOCKED UserDetails when account is blocked")
    void should_return_account_locked_when_blocked() {
        final var accountDto = validAccountDto()
            .email(new EmailAddress(RandomEmailUtils.generateValidEmail()))
            .isVerified(true)
            .isDeleted(false)
            .isBlocked(true)
            .build();

        when(findAccount.findAccountForAuthentication(accountDto.email().value()))
            .thenReturn(accountDto);
        when(findProfile.findByAccountEmail(accountDto.email().value()))
            .thenReturn(List.of());

        final var result = customUserDetailsService.loadUserByUsername(accountDto.email().value());

        assertThat(result).isNotNull();
        assertThat(result.isAccountNonLocked()).isFalse();
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should return DISABLED UserDetails when account is deleted")
    void should_return_account_disabled_when_deleted() {
        final var accountDto = validAccountDto()
            .email(new EmailAddress(RandomEmailUtils.generateValidEmail()))
            .isVerified(true)
            .isDeleted(true)
            .isBlocked(false)
            .build();

        when(findAccount.findAccountForAuthentication(accountDto.email().value()))
            .thenReturn(accountDto);
        when(findProfile.findByAccountEmail(accountDto.email().value()))
            .thenReturn(List.of());

        final var result = customUserDetailsService.loadUserByUsername(accountDto.email().value());

        assertThat(result).isNotNull();
        assertThat(result.isEnabled()).isFalse();
        assertThat(result.isAccountNonLocked()).isTrue();
    }

    @Test
    @DisplayName("Should map multiple profiles correctly to authorities")
    void should_map_multiple_profiles() {
        AccountDto accountDto = validAccountDto()
            .isVerified(true)
            .isBlocked(false).isDeleted(false)
            .build();

        final var shelter = ProfileBuilder
            .profileDto()
            .profileType(ProfileTypeEnum.SHELTER)
            .isDeleted(false).build();

        final var admin = ProfileBuilder
            .profileDto()
            .profileType(ProfileTypeEnum.ADMIN)
            .isDeleted(false).build();

        when(findAccount.findAccountForAuthentication(accountDto.email().value()))
            .thenReturn(accountDto);
        when(findProfile.findByAccountEmail(accountDto.email().value()))
            .thenReturn(List.of(shelter, admin));

        UserDetails result = customUserDetailsService
            .loadUserByUsername(accountDto.email().value());

        assertThat(result.getAuthorities())
            .extracting(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder(
                "ROLE_USER",
                "SCOPE_VERIFIED",
                "ROLE_SHELTER",
                "ROLE_ADMIN"
            );
    }
}
