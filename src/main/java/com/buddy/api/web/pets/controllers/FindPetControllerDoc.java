package com.buddy.api.web.pets.controllers;

import com.buddy.api.web.advice.error.ErrorResponse;
import com.buddy.api.web.pets.requests.PetSearchCriteriaRequest;
import com.buddy.api.web.pets.responses.PetParamsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;

@Tag(name = "Pet", description = "Endpoints related to pets")
public interface FindPetControllerDoc {

    @Operation(summary = "Get pets with pagination",
        description = "Get pets based on search criteria with pagination. "
            + "You can provide various search parameters to filter the results.", responses = {
                @ApiResponse(responseCode = "200", description = "Pets found successfully"),

                @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
                    @Schema(implementation = ErrorResponse.class)))})

    PagedModel<EntityModel<PetParamsResponse>> findPetsBySearchParams(
        @Parameter(description = "Search criteria for filtering pets")
        PetSearchCriteriaRequest petSearchCriteriaRequest,
        @Parameter(description = "Pagination information") Pageable pageable);
}
