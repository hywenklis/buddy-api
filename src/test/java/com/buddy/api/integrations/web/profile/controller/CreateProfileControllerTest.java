package com.buddy.api.integrations.web.profile.controller;

import static com.buddy.api.builders.account.AccountBuilder.validAccountEntity;
import static com.buddy.api.builders.profile.ProfileBuilder.profileRequest;
import static com.buddy.api.customverifications.CustomCreatedVerifications.expectCreatedFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectBadRequestFrom;
import static com.buddy.api.customverifications.CustomErrorVerifications.expectNotFoundFrom;
import static com.buddy.api.utils.RandomStringUtils.generateRandomString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.buddy.api.domains.account.entities.AccountEntity;
import com.buddy.api.domains.profile.entities.ProfileEntity;
import com.buddy.api.integrations.IntegrationTestAbstract;
import com.buddy.api.web.profiles.requests.ProfileRequest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("POST /v1/profiles/register")
class CreateProfileControllerTest extends IntegrationTestAbstract {

    private static final String PROFILE_REGISTER_URL = "/v1/profiles/register";
    private static final String ERROR_ACCOUNT_ID_REQUIRED = "Profile account ID is mandatory";
    private static final String ERROR_ACCOUNT_NOT_FOUND = "Account not found";
    private static final String ERROR_NAME_REQUIRED = "Profile name is mandatory";
    private static final String ERROR_NAME_SIZE =
        "Profile name must have between 3 and 100 characters";
    private static final String ERROR_DESCRIPTION_SIZE =
        "Profile description must have at most 255 characters";
    private static final String ERROR_PROFILE_TYPE_REQUIRED = "Profile type is mandatory";

    @Test
    @DisplayName("Should register a new profile successfully")
    void should_register_new_profile() throws Exception {
        var accountEntity = accountRepository.save(validAccountEntity().build());

        var request = profileComponent
            .validProfileRequest()
            .accountId(accountEntity.getAccountId())
            .build();

        var response = performCreateProfileRequest(request);

        expectCreatedFrom(response);

        assertProfileCount(accountEntity, 1);

        var newProfile = findProfileByName(accountEntity, request.name());
        assertProfileDetails(newProfile, request);
    }

    @Test
    @DisplayName("Should register a new profile for an account that already has one")
    void should_register_new_profile_for_account_with_existing_profile() throws Exception {
        var accountEntity = accountRepository.save(validAccountEntity().build());

        var firstProfileRequest = profileComponent
            .validProfileRequest()
            .accountId(accountEntity.getAccountId())
            .build();

        var firstProfileResponse = performCreateProfileRequest(firstProfileRequest);
        expectCreatedFrom(firstProfileResponse);
        assertProfileCount(accountEntity, 1);

        var secondProfileRequest = profileComponent
            .validProfileRequest()
            .accountId(accountEntity.getAccountId())
            .name("Second Profile Name")
            .build();

        var secondProfileResponse = performCreateProfileRequest(secondProfileRequest);

        expectCreatedFrom(secondProfileResponse);
        assertProfileCount(accountEntity, 2);

        var newProfile = findProfileByName(accountEntity, secondProfileRequest.name());
        assertProfileDetails(newProfile, secondProfileRequest);
    }

    @Test
    @DisplayName("Should not create profile without account id")
    void should_not_create_profile_without_account_id() throws Exception {
        final var request = profileRequest().accountId(null).build();

        expectBadRequestFrom(performCreateProfileRequest(request))
            .forField("accountId", ERROR_ACCOUNT_ID_REQUIRED);
    }

    @Test
    @DisplayName("Should not create profile when account id is not from an account in database")
    void should_not_create_profile_when_account_is_not_from_database_account() throws Exception {
        final var request = profileRequest().accountId(UUID.randomUUID()).build();

        expectNotFoundFrom(performCreateProfileRequest(request))
            .forField("accountId", ERROR_ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("Should not create profile when account is marked for deletion")
    void should_not_create_profile_when_account_is_marked_for_deletion() throws Exception {

        var accountEntity = accountRepository.save(validAccountEntity().isDeleted(true).build());
        var accountId = accountEntity.getAccountId();

        final var request = profileRequest().accountId(accountId).build();

        expectNotFoundFrom(performCreateProfileRequest(request))
            .forField("accountId", ERROR_ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("Should not create profile without name")
    void should_not_create_profile_without_name() throws Exception {
        final var request = profileComponent
            .validProfileRequest()
            .name(null)
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request))
            .forField("name", ERROR_NAME_REQUIRED);
    }

    @Test
    @DisplayName("Should not create profile when name is too small")
    void should_not_create_profile_when_name_is_too_small() throws Exception {
        final var request = profileComponent
            .validProfileRequest()
            .name(generateRandomString(2))
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request))
            .forField("name", ERROR_NAME_SIZE);
    }

    @Test
    @DisplayName("Should not create profile when name is too big")
    void should_not_create_profile_when_name_is_too_big() throws Exception {
        final var request = profileComponent
            .validProfileRequest()
            .name(generateRandomString(101))
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request))
            .forField("name", ERROR_NAME_SIZE);
    }

    @Test
    @DisplayName("Should not create profile when description is too big")
    void should_not_create_profile_when_description_too_big() throws Exception {
        final var request = profileComponent
            .validProfileRequest()
            .description(generateRandomString(256))
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request))
            .forField("description", ERROR_DESCRIPTION_SIZE);
    }

    @Test
    @DisplayName("Should not create profile without profile type")
    void should_not_create_profile_without_profile_type() throws Exception {
        final var request = profileComponent
            .validProfileRequest()
            .profileType(null)
            .build();

        expectBadRequestFrom(performCreateProfileRequest(request))
            .forField("profileType", ERROR_PROFILE_TYPE_REQUIRED);
    }

    private ResultActions performCreateProfileRequest(final ProfileRequest request)
        throws Exception {
        return mockMvc
            .perform(post(PROFILE_REGISTER_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private void assertProfileCount(final AccountEntity account, final int expectedCount) {
        var profiles = profileRepository.findByAccount(account);
        assertThat(profiles).hasSize(expectedCount);
    }

    private ProfileEntity findProfileByName(final AccountEntity account, final String name) {
        return profileRepository.findByAccount(account).stream()
            .filter(profile -> profile.getName().equals(name))
            .findFirst().get();
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
