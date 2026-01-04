package com.buddy.api.integrations;

import com.buddy.api.commons.configurations.security.jwt.JwtUtil;
import com.buddy.api.components.AccountComponent;
import com.buddy.api.components.PetComponent;
import com.buddy.api.components.ProfileComponent;
import com.buddy.api.components.ShelterComponent;
import com.buddy.api.domains.account.repositories.AccountRepository;
import com.buddy.api.domains.adoption.repositories.AdoptionRequestRepository;
import com.buddy.api.domains.pet.repositories.PetImageRepository;
import com.buddy.api.domains.pet.repositories.PetRepository;
import com.buddy.api.domains.profile.repositories.ProfileRepository;
import com.buddy.api.domains.shelter.entities.ShelterEntity;
import com.buddy.api.integrations.configs.RedisTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0, stubs = "classpath:/mappings")
@Import(RedisTestConfig.class)
public abstract class IntegrationTestAbstract {

    @Autowired
    protected CacheManager cacheManager;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AccountComponent accountComponent;

    @Autowired
    protected ShelterComponent shelterComponent;

    @Autowired
    protected PetComponent petComponent;

    @Autowired
    protected ProfileComponent profileComponent;

    @Autowired
    protected ProfileRepository profileRepository;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected PetRepository petRepository;

    @Autowired
    protected PetImageRepository petImageRepository;

    @Autowired
    protected AdoptionRequestRepository adoptionRequestRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtUtil jwtUtil;

    @Autowired
    protected RedisTemplate<String, String> redisTemplate;

    protected static final String PET_BASE_URL = "/v1/pets";
    protected static final String EMBEDDED_PET_RESPONSES = "$._embedded.petParamsResponseList";
    protected static final String AUTH_URL = "/v1/auth/login";
    protected static final String REFRESH_URL = "/v1/auth/refresh";
    protected static final String ACCESS_TOKEN_NAME = "access_token";
    protected static final String REFRESH_TOKEN_NAME = "refresh_token";
    protected static final String ORIGIN = "Origin";
    protected static final String CREDENTIALS_NAME = "credentials";
    protected static final String VERIFICATION_URL = "/v1/accounts/verifications";
    protected static final String MANAGER_NOTIFICATION_API_URL = "/manager/v1/gateway";
    protected static final String MANAGER_API_URL = "/manager/v1/auth/login";
    protected static final String PATH_EMAIL_VERIFICATION_REQUEST = "/request";
    protected static final String PATH_EMAIL_VERIFICATION_CONFIRM = "/confirm";
    protected static final String BEARER = "Bearer ";
    protected static final String EMAIL = "email";

    protected ShelterEntity shelter;

    protected void clearRepositories() {
        petImageRepository.deleteAll();
        adoptionRequestRepository.deleteAll();
        petRepository.deleteAll();
    }

    @BeforeEach
    void init() {
        clearRepositories();

        redisTemplate.execute((RedisConnection connection) -> {
            connection.serverCommands().flushDb();
            return "OK";
        });

        WireMock.resetAllRequests();
        WireMock.resetAllScenarios();
    }
}
