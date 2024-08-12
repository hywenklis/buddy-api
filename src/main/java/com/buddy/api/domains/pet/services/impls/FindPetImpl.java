package com.buddy.api.domains.pet.services.impls;

import com.buddy.api.domains.exceptions.PetSearchException;
import com.buddy.api.domains.page.PageableBuilder;
import com.buddy.api.domains.pet.dtos.PetSearchCriteriaDto;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.domains.pet.mappers.PetDomainMapper;
import com.buddy.api.domains.pet.repositories.PetRepository;
import com.buddy.api.domains.pet.services.FindPet;
import com.buddy.api.domains.specifications.PetSpecifications;
import com.buddy.api.web.pets.requests.PetSearchCriteriaRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindPetImpl implements FindPet {

    private final PetRepository petRepository;
    private final PetDomainMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PetSearchCriteriaDto> findPets(final PetSearchCriteriaRequest searchParams,
                                               final Pageable pageable) {
        try {
            Specification<PetEntity> spec = PetSpecifications.withParams(searchParams);
            Pageable adjustedPageable = PageableBuilder.buildPageable(pageable);
            Page<PetEntity> petPage = petRepository.findAll(spec, adjustedPageable);

            if (petPage.isEmpty()) {
                return Page.empty();
            }

            List<PetSearchCriteriaDto> dtos = petPage.stream()
                .map(mapper::mapParamsToDto)
                .toList();

            return new PageImpl<>(dtos, adjustedPageable, petPage.getTotalElements());
        } catch (PropertyReferenceException ex) {
            throw new PetSearchException(
                ex.getPropertyName(),
                "An error occurred while searching for pets due to an invalid property reference",
                ex
            );
        }
    }
}
