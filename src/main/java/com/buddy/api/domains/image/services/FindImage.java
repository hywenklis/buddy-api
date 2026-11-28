package com.buddy.api.domains.image.services;

import com.buddy.api.domains.image.entities.ImageEntity;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import java.util.List;
import java.util.UUID;

public interface FindImage {

    List<ImageEntity> findByPetV2OrderByDisplayOrderAsc(PetV2Entity petV2);

    List<ImageEntity> findByPetV2_PetV2IdInOrderByDisplayOrderAsc(List<UUID> petV2Ids);
}
