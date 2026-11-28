package com.buddy.api.domains.image.repositories;

import com.buddy.api.domains.image.entities.ImageEntity;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {

    @Query("SELECT i FROM ImageEntity i WHERE i.petV2 = :petV2 ORDER BY i.displayOrder ASC")
    List<ImageEntity> findByPetV2OrderByDisplayOrderAsc(@Param("petV2") PetV2Entity petV2);

    @Query("SELECT i FROM ImageEntity i WHERE i.petV2.petV2Id IN :petV2Ids "
        + "ORDER BY i.displayOrder ASC")
    List<ImageEntity> findByPetV2_PetV2IdInOrderByDisplayOrderAsc(
        @Param("petV2Ids") List<UUID> petV2Ids
    );
}
