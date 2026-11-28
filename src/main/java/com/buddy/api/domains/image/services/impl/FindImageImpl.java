package com.buddy.api.domains.image.services.impl;

import com.buddy.api.domains.image.entities.ImageEntity;
import com.buddy.api.domains.image.repositories.ImageRepository;
import com.buddy.api.domains.image.services.FindImage;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindImageImpl implements FindImage {

    private final ImageRepository imageRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ImageEntity> findByPetV2OrderByDisplayOrderAsc(final PetV2Entity petV2) {
        log.info("Fetching images for pet '{}'", petV2 != null ? petV2.getPetV2Id() : null);
        if (petV2 == null) {
            return List.of();
        }
        return imageRepository.findByPetV2OrderByDisplayOrderAsc(petV2);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageEntity> findByPetV2_PetV2IdInOrderByDisplayOrderAsc(
        final List<UUID> petV2Ids
    ) {
        log.info("Fetching images for pet IDs '{}'", petV2Ids);
        if (petV2Ids == null || petV2Ids.isEmpty()) {
            return List.of();
        }
        return imageRepository.findByPetV2_PetV2IdInOrderByDisplayOrderAsc(petV2Ids);
    }
}
