package com.buddy.api.web.pets.v2.controllers;

import com.buddy.api.commons.configurations.cache.annotations.RateLimited;
import com.buddy.api.commons.page.PageResponse;
import com.buddy.api.domains.authentication.dtos.AuthenticatedUser;
import com.buddy.api.domains.pet.services.v2.CreatePetV2;
import com.buddy.api.domains.pet.services.v2.FindPetV2;
import com.buddy.api.domains.pet.services.v2.GetPetV2;
import com.buddy.api.domains.pet.services.v2.UpdatePetV2;
import com.buddy.api.web.defaultresponses.CreatedSuccessResponse;
import com.buddy.api.web.pets.v2.mappers.PetV2RequestMapper;
import com.buddy.api.web.pets.v2.mappers.PetV2ResponseMapper;
import com.buddy.api.web.pets.v2.requests.CreatePetV2Request;
import com.buddy.api.web.pets.v2.requests.PetV2SearchCriteriaRequest;
import com.buddy.api.web.pets.v2.requests.UpdatePetV2Request;
import com.buddy.api.web.pets.v2.responses.PetV2Response;
import com.buddy.api.web.pets.v2.responses.PetV2SummaryResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/pets")
@RequiredArgsConstructor
public class PetV2Controller implements PetV2ControllerDoc {

    private final CreatePetV2 createPetService;
    private final FindPetV2 findPetService;
    private final GetPetV2 getPetService;
    private final UpdatePetV2 updatePetService;
    private final PetV2RequestMapper requestMapper;
    private final PetV2ResponseMapper responseMapper;

    @Override
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SHELTER') and hasAuthority('SCOPE_VERIFIED')")
    @RateLimited(
        useIp = true,
        operation = "createPetV2",
        limitMessage =
            "Too many pet registration requests. Please wait a minute before trying again."
    )
    public CreatedSuccessResponse registerPet(
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @RequestBody @Valid final CreatePetV2Request request
    ) {
        final var dto = requestMapper.toCreateDto(request, authenticatedUser.getAccountId());
        createPetService.create(dto);
        return new CreatedSuccessResponse();
    }

    @Override
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<PetV2SummaryResponse> findPets(
        final PetV2SearchCriteriaRequest criteria,
        final Pageable pageable
    ) {
        final var criteriaDto = requestMapper.toSearchCriteriaDto(criteria);
        final var pageResult = findPetService.findPets(criteriaDto, pageable);

        final var summaryContent = pageResult.content().stream()
            .map(responseMapper::toSummaryResponse)
            .toList();

        return new PageResponse<>(
            summaryContent,
            pageResult.page(),
            pageResult.size(),
            pageResult.totalElements(),
            pageResult.totalPages(),
            pageResult.isFirst(),
            pageResult.isLast(),
            pageResult.hasNext()
        );
    }

    @Override
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PetV2Response findPetById(@PathVariable final UUID id) {
        final var petDto = getPetService.findById(id);
        return responseMapper.toResponse(petDto);
    }

    @Override
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SHELTER') and hasAuthority('SCOPE_VERIFIED')")
    public PetV2Response updatePet(
        @PathVariable final UUID id,
        @AuthenticationPrincipal final AuthenticatedUser authenticatedUser,
        @RequestBody @Valid final UpdatePetV2Request request
    ) {
        final var updateDto = requestMapper.toUpdateDto(id, request);
        final var updated = updatePetService
            .update(updateDto, authenticatedUser.getAccountId(), authenticatedUser.isAdmin());
        return responseMapper.toResponse(updated);
    }
}
