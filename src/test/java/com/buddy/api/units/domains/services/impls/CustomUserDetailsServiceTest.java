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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;

class CustomUserDetailsServiceTest extends UnitTestAbstract {

    @Mock
    private FindAccount findAccount;

    @Mock
    private FindProfile findProfile;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Should load user details successfully when account and profiles are found")
    void should_load_user_details_successfully() {
        AccountDto accountDto = validAccountDto()
            .email(new EmailAddress(RandomEmailUtils.generateValidEmail()))
            .isBlocked(false)
            .isDeleted(false)
            .isVerified(true)
            .build();

        ProfileDto activeProfile = ProfileBuilder.profileDto().isDeleted(false).build();
        ProfileDto deletedProfile = ProfileBuilder.profileDto().isDeleted(true).build();

        List<ProfileDto> profiles = List.of(activeProfile, deletedProfile);

        when(findAccount.findAccountForAuthentication(accountDto.email().value())).thenReturn(
            accountDto);
        when(findProfile.findByAccountEmail(accountDto.email().value())).thenReturn(profiles);

        UserDetails result = customUserDetailsService.loadUserByUsername(
            accountDto.email().value()
        );

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(accountDto.email().value());
        assertThat(result.getPassword()).isEqualTo(accountDto.password());
        assertThat(result.getAuthorities().size() == 1).isSameAs(true);
        assertThat(result.getAuthorities().iterator().next().getAuthority())
            .isEqualTo("ROLE_" + activeProfile.profileType().name());
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should return user with no authorities when all profiles are deleted")
    void should_return_user_with_no_authorities_when_all_profiles_are_deleted() {
        AccountDto accountDto = validAccountDto()
            .email(new EmailAddress(RandomEmailUtils.generateValidEmail()))
            .isVerified(false)
            .isDeleted(false)
            .isBlocked(false)
            .build();

        ProfileDto deletedProfile = ProfileBuilder.profileDto().isDeleted(true).build();
        List<ProfileDto> profiles = List.of(deletedProfile);

        when(findAccount.findAccountForAuthentication(accountDto.email().value())).thenReturn(
            accountDto);
        when(findProfile.findByAccountEmail(accountDto.email().value())).thenReturn(profiles);

        UserDetails result = customUserDetailsService.loadUserByUsername(
            accountDto.email().value()
        );

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(accountDto.email().value());
        assertThat(result.getPassword()).isEqualTo(accountDto.password());
        assertThat(result.getAuthorities().size()).isZero();
    }

    @ParameterizedTest
    @CsvSource({
        "true, false",
        "false, true",
        "true, true"
    })
    @DisplayName("then it should return disabled UserDetails if account is deleted or blocked")
    void thenShouldReturnDisabledUserDetails(final Boolean isDeleted, final Boolean isBlocked) {
        final var accountDto = validAccountDto()
            .email(new EmailAddress(RandomEmailUtils.generateValidEmail()))
            .isVerified(true)
            .isDeleted(isDeleted)
            .isBlocked(isBlocked)
            .build();

        when(findAccount.findAccountForAuthentication(accountDto.email().value()))
            .thenReturn(accountDto);

        when(findProfile.findByAccountEmail(accountDto.email().value())).thenReturn(List.of());

        final var result = customUserDetailsService.loadUserByUsername(accountDto.email().value());

        assertThat(result).isNotNull();
        assertThat(result.isAccountNonLocked()).isFalse();
    }

    @Test
    @DisplayName("Should map profile types to authorities correctly")
    void should_map_authorities_correctly() {
        final var account = validAccountDto()
            .email(new EmailAddress(RandomEmailUtils.generateValidEmail()))
            .isVerified(true)
            .isBlocked(false)
            .isDeleted(false)
            .build();

        final var profileUser = ProfileDto.builder()
            .profileType(ProfileTypeEnum.USER)
            .isDeleted(false)
            .build();

        final var profileAdmin = ProfileDto.builder()
            .profileType(ProfileTypeEnum.ADMIN)
            .isDeleted(false)
            .build();

        when(findAccount.findAccountForAuthentication(account.email().value())).thenReturn(account);
        when(findProfile.findByAccountEmail(account.email().value())).thenReturn(
            List.of(profileUser, profileAdmin));

        final var userDetails =
            customUserDetailsService.loadUserByUsername(account.email().value());

        assertThat(userDetails.getAuthorities())
            .extracting("authority")
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}
