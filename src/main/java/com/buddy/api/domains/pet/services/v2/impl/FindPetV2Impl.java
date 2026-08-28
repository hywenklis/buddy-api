package com.buddy.api.domains.pet.services.v2.impl;

import com.buddy.api.commons.page.PageResponse;
import com.buddy.api.domains.image.entities.ImageEntity;
import com.buddy.api.domains.image.services.FindImage;
import com.buddy.api.domains.pet.dtos.v2.PetV2Dto;
import com.buddy.api.domains.pet.dtos.v2.PetV2SearchCriteriaDto;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import com.buddy.api.domains.pet.mappers.v2.PetV2DomainMapper;
import com.buddy.api.domains.pet.repositories.PetV2Repository;
import com.buddy.api.domains.pet.services.v2.FindPetV2;
import com.buddy.api.domains.pet.specifications.v2.PetV2Specifications;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindPetV2Impl implements FindPetV2 {

    private final PetV2Repository petV2Repository;
    private final FindImage findImage;
    private final PetV2DomainMapper domainMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PetV2Dto> findPets(final PetV2SearchCriteriaDto criteria,
                                           final Pageable pageable) {
        log.info("Searching pets with criteria '{}' and pageable '{}'", criteria, pageable);

        final var adjustedPageable = pageable.getSort().isUnsorted()
            ? PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(Sort.Direction.DESC, "creationDate")
        )
            : pageable;

        final var spec = PetV2Specifications.withCriteria(criteria);
        final var page = petV2Repository.findAll(spec, adjustedPageable);

        if (page.isEmpty()) {
            return PageResponse.of(page, domainMapper::toDto);
        }

        final var petIds = page.getContent().stream()
            .map(PetV2Entity::getPetV2Id)
            .toList();

        final var images = findImage.findByPetV2_PetV2IdInOrderByDisplayOrderAsc(petIds);
        final Map<UUID, List<ImageEntity>> imagesByPetId = images.stream()
            .filter(img -> img.getPetV2() != null)
            .collect(Collectors.groupingBy(img -> img.getPetV2().getPetV2Id()));

        return PageResponse.of(
            page,
            pet -> domainMapper.toDto(pet, imagesByPetId.getOrDefault(pet.getPetV2Id(), List.of()))
        );
    }
}
