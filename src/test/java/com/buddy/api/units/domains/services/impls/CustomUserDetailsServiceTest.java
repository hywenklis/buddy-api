package com.buddy.api.units.domains.services.impls;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import com.buddy.api.builders.account.AccountBuilder;
import com.buddy.api.builders.profile.ProfileBuilder;
import com.buddy.api.domains.account.dtos.AccountDto;
import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.authentication.services.impls.CustomUserDetailsService;
import com.buddy.api.domains.profile.dtos.ProfileDto;
import com.buddy.api.domains.profile.services.FindProfile;
import com.buddy.api.units.UnitTestAbstract;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
        AccountDto accountDto = AccountBuilder.validAccountDto()
            .isBlocked(false)
            .isDeleted(false)
            .isVerified(true)
            .build();

        ProfileDto activeProfile = ProfileBuilder.profileDto().isDeleted(false).build();
        ProfileDto deletedProfile = ProfileBuilder.profileDto().isDeleted(true).build();

        List<ProfileDto> profiles = List.of(activeProfile, deletedProfile);

        when(findAccount.findByEmail(accountDto.email().value())).thenReturn(accountDto);
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
        AccountDto accountDto = AccountBuilder.validAccountDto()
            .isDeleted(false)
            .isBlocked(false)
            .build();

        ProfileDto deletedProfile = ProfileBuilder.profileDto().isDeleted(true).build();
        List<ProfileDto> profiles = List.of(deletedProfile);

        when(findAccount.findByEmail(accountDto.email().value())).thenReturn(accountDto);
        when(findProfile.findByAccountEmail(accountDto.email().value())).thenReturn(profiles);

        UserDetails result = customUserDetailsService.loadUserByUsername(
            accountDto.email().value()
        );

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(accountDto.email().value());
        assertThat(result.getPassword()).isEqualTo(accountDto.password());
        assertThat(result.getAuthorities().size()).isZero();
    }
}
