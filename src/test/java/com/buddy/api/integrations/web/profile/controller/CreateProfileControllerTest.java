package com.buddy.api.integrations.web.profile.controller;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static com.buddy.api.builders.profile.ProfileBuilder.profileRequest;
import static com.buddy.api.customverifications.CustomCreatedVerifications.expectCreatedFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectErrorStatusFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectNotFoundFrom;
import static com.buddy.api.domains.profile.enums.ProfileTypeEnum.ADMIN;
import static com.buddy.api.domains.profile.enums.ProfileTypeEnum.SHELTER;
import static com.buddy.api.domains.profile.enums.ProfileTypeEnum.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.profiles.requests.ProfileRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("POST /v1/profiles/register")
class CreateProfileControllerTest extends IntegrationTestAbstract {

    private static final String PROFILE_REGISTER_URL = "/v1/profiles/register";
    private static final String VALID_ORIGIN = "550e8400-e29b-41d4-a716-446655440000";
    private static final String FIELD_NAME = "name";
    private static final String ERROR_ACCOUNT_ID_REQUIRED = "Profile account ID is mandatory";
    private static final String ERROR_ACCOUNT_NOT_FOUND = "Account not found";
    private static final String ERROR_NAME_REQUIRED = "Profile name is mandatory";
    private static final String ERROR_REPEATED_NAME = "Profile name already registered";
    private static final String ERROR_PROFILE_TYPE_REQUIRED = "Profile type is mandatory";

    private static final String ERROR_NAME_SIZE =
        "Profile name must have between 3 and 100 characters";

    private static final String ERROR_DESCRIPTION_SIZE =
        "Profile description must have at most 255 characters";

    private static final String ERROR_PROFILE_TYPE_ADMIN_FORBIDDEN =
        "Profile type ADMIN cannot be created by user";

    private AccountEntity authenticatedAccount;
    private String accessToken;

    @BeforeEach
    void setUp() {
        authenticatedAccount =
            accountRepository.save(validAccountEntity().isVerified(true).build());

        accessToken = jwtUtil.generateAccessToken(
            authenticatedAccount.getEmail().value(),
            List.of(
                "ROLE_" + USER.name(),
                "ROLE_" + ADMIN.name(),
                "ROLE_" + SHELTER.name(),
                "SCOPE_VERIFIED"
            )
        );
    }

    @Test
    @DisplayName("Should register a new profile successfully")
    void should_register_new_profile() throws Exception {
        final var request = profileComponent.validProfileRequest()
            .accountId(authenticatedAccount.getAccountId())
            .build();

        ResultActions response = performCreateProfileRequest(request, accessToken);

        final var newProfile = findProfileByName(authenticatedAccount, request.name());

        expectCreatedFrom(response);
        assertProfileCount(authenticatedAccount, 1);
        assertProfileDetails(newProfile.get(), request);
    }

    @Test
    @DisplayName("Should NOT register profile if user is NOT VERIFIED (Missing Scope)")
    void should_not_register_profile_if_unverified() throws Exception {
        final var unverifiedAccount = accountRepository.save(
            validAccountEntity()
                .isVerified(false)
                .build()
        );

        String unverifiedToken = jwtUtil.generateAccessToken(
            unverifiedAccount.getEmail().value(),
            List.of("ROLE_USER")
        );

        final var request = profileComponent.validProfileRequest()
            .accountId(unverifiedAccount.getAccountId())
            .build();

        ResultActions response = performCreateProfileRequest(request, unverifiedToken);

        expectErrorStatusFrom(response, HttpStatus.FORBIDDEN)
            .forField("authorization",
                "Access denied you are not allowed to perform this action");

        assertProfileCount(unverifiedAccount, 0);
    }

    @Test
    @DisplayName("Should register a new profile for an account that already has one")
    void should_register_new_profile_for_account_with_existing_profile() throws Exception {
        final var firstRequest = profileComponent.validProfileRequest()
            .accountId(authenticatedAccount.getAccountId())
            .build();

        ResultActions firstResponse = performCreateProfileRequest(firstRequest, accessToken);

        expectCreatedFrom(firstResponse);
        assertProfileCount(authenticatedAccount, 1);

        final var secondRequest = profileComponent.validProfileRequest()
            .accountId(authenticatedAccount.getAccountId())
            .build();

        ResultActions secondResponse = performCreateProfileRequest(secondRequest, accessToken);
        final var profileEntity = findProfileByName(authenticatedAccount, secondRequest.name());

        expectCreatedFrom(secondResponse);
        assertProfileCount(authenticatedAccount, 2);
        assertProfileDetails(profileEntity.get(), secondRequest);
    }

    @Test
    @DisplayName("Should not create profile without account id")
    void should_not_create_profile_without_account_id() throws Exception {
        final var request = profileRequest().accountId(null).build();

        expectBadRequestFrom(performCreateProfileRequest(request, accessToken))
            .forField("accountId", ERROR_ACCOUNT_ID_REQUIRED);
    }

    @Test
    @DisplayName("Should not create profile when account id is not from an account in database")
    void should_not_create_profile_when_account_is_not_from_database_account() throws Exception {
        final var request = profileRequest().accountId(UUID.randomUUID()).build();

        expectNotFoundFrom(performCreateProfileRequest(request, accessToken))
            .forField("accountId", ERROR_ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("Should not create profile when account is marked for deletion")
    void should_not_create_profile_when_account_is_marked_for_deletion() throws Exception {
        final var deletedAccount =
            accountRepository.save(validAccountEntity().isDeleted(true).build());
        final var request = profileComponent.validProfileRequest()
            .accountId(deletedAccount.getAccountId())
            .build();

        expectNotFoundFrom(performCreateProfileRequest(request, accessToken))
            .forField("accountId", ERROR_ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("Should not create profile without name")
    void should_not_create_profile_without_name() throws Exception {
        final var request = profileComponent.validProfileRequest()
            .accountId(authenticatedAccount.getAccountId())
            .name(null)
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request, accessToken))
            .forField(FIELD_NAME, ERROR_NAME_REQUIRED);
    }

    @Test
    @DisplayName("Should not create profile when name is too small")
    void should_not_create_profile_when_name_is_too_small() throws Exception {
        final var request = profileComponent.validProfileRequest()
            .accountId(authenticatedAccount.getAccountId())
            .name(RandomStringUtils.secure().nextAlphabetic(2))
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request, accessToken))
            .forField(FIELD_NAME, ERROR_NAME_SIZE);
    }

    @Test
    @DisplayName("Should not create profile when name is too big")
    void should_not_create_profile_when_name_is_too_big() throws Exception {
        final var request = profileComponent.validProfileRequest()
            .accountId(authenticatedAccount.getAccountId())
            .name(RandomStringUtils.secure().nextAlphabetic(101))
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request, accessToken))
            .forField(FIELD_NAME, ERROR_NAME_SIZE);
    }

    @Test
    @DisplayName("Should not create profile when name is already in database")
    void should_not_create_profile_when_name_is_already_in_database() throws Exception {
        String name = RandomStringUtils.secure().nextAlphabetic(10);
        profileRepository.save(profileComponent.validProfileEntity().name(name).build());

        final var request = profileComponent.validProfileRequest()
            .accountId(authenticatedAccount.getAccountId())
            .name(name)
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request, accessToken))
            .forField(FIELD_NAME, ERROR_REPEATED_NAME);
        assertProfileCount(authenticatedAccount, 0);
    }

    @Test
    @DisplayName("Should not create profile when description is too big")
    void should_not_create_profile_when_description_too_big() throws Exception {
        final var request = profileComponent.validProfileRequest()
            .accountId(authenticatedAccount.getAccountId())
            .description(RandomStringUtils.secure().nextAlphabetic(256))
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request, accessToken))
            .forField("description", ERROR_DESCRIPTION_SIZE);
    }

    @Test
    @DisplayName("Should not create profile without profile type")
    void should_not_create_profile_without_profile_type() throws Exception {
        final var request = profileComponent.validProfileRequest()
            .accountId(authenticatedAccount.getAccountId())
            .profileType(null)
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request, accessToken))
            .forField("profileType", ERROR_PROFILE_TYPE_REQUIRED);
    }

    @Test
    @DisplayName("Should not create ADMIN profile")
    void should_not_create_admin_profile() throws Exception {
        final var request = profileComponent.validProfileRequest()
            .accountId(authenticatedAccount.getAccountId())
            .profileType(ADMIN)
            .build();

        expectErrorStatusFrom(performCreateProfileRequest(request, accessToken),
            HttpStatus.FORBIDDEN)
            .forField("profileType", ERROR_PROFILE_TYPE_ADMIN_FORBIDDEN);
        assertProfileCount(authenticatedAccount, 0);
    }

    private ResultActions performCreateProfileRequest(final ProfileRequest request,
                                                      final String token)
        throws Exception {
        return mockMvc
            .perform(post(PROFILE_REGISTER_URL)
                .header("Authorization", "Bearer " + token)
                .header("Origin", VALID_ORIGIN)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private void assertProfileCount(final AccountEntity account, final int expectedCount) {
        final var profiles = profileRepository.findByAccountEmail(account.getEmail());
        assertThat(profiles).isPresent();
        assertThat(profiles.get()).hasSize(expectedCount);
    }

    private Optional<ProfileEntity> findProfileByName(final AccountEntity account,
                                                      final String name
    ) {
        return profileRepository.findByAccountEmail(account.getEmail()).get().stream()
            .filter(profile -> profile.getName().equals(name))
            .findFirst();
    }

    private void assertProfileDetails(final ProfileEntity profile, final ProfileRequest request) {
        assertAll(
            "Validating ProfileEntity details",
            () -> assertThat(profile.getProfileId()).isNotNull(),
            () -> assertThat(profile.getAccount().getAccountId()).isEqualTo(request.accountId()),
            () -> assertThat(profile.getName()).isEqualTo(request.name()),
            () -> assertThat(profile.getDescription()).isEqualTo(request.description()),
            () -> assertThat(profile.getProfileType()).isEqualTo(request.profileType()),
            () -> assertThat(profile.getIsDeleted()).isFalse(),
            () -> assertThat(profile.getCreationDate()).isNotNull(),
            () -> assertThat(profile.getUpdatedDate()).isNotNull()
        );
    }
}
