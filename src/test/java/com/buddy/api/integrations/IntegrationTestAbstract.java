package com.buddy.api.integrations;

import com.buddy.api.components.ShelterComponent;
import com.buddy.api.domains.adoption.repositories.AdoptionRequestRepository;
import com.buddy.api.domains.pet.repositories.PetImageRepository;
import com.buddy.api.domains.pet.repositories.PetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class IntegrationTestAbstract {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ShelterComponent shelterComponent;

    @Autowired
    protected PetRepository petRepository;

    @Autowired
    protected PetImageRepository petImageRepository;

    @Autowired
    protected AdoptionRequestRepository adoptionRequestRepository;
}
