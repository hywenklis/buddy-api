package com.buddy.api.web.pets.v2.controllers;

import com.buddy.api.commons.page.PageResponse;
import com.buddy.api.domains.authentication.dtos.AuthenticatedUser;
import com.buddy.api.web.advice.error.ErrorResponse;
import com.buddy.api.web.defaultresponses.CreatedSuccessResponse;
import com.buddy.api.web.pets.v2.requests.CreatePetV2Request;
import com.buddy.api.web.pets.v2.requests.PetV2SearchCriteriaRequest;
import com.buddy.api.web.pets.v2.requests.UpdatePetV2Request;
import com.buddy.api.web.pets.v2.responses.PetV2Response;
import com.buddy.api.web.pets.v2.responses.PetV2SummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

@Tag(name = "Pets V2", description = "Endpoints for pet management and public search (Version 2)")
public interface PetV2ControllerDoc {

    @Operation(
        summary = "Register a new pet (V2)",
        description = "Registers a pet linked to the authenticated guardian profile."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pet registered successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CreatedSuccessResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error in request payload",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
            description = "Unauthorized - Invalid authentication credentials",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Missing token, unverified account, or requires shelter role",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Unprocessable Entity - Inactive or non-existent shelter profile",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "429", description = "Too many requests",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    CreatedSuccessResponse registerPet(
        AuthenticatedUser authenticatedUser,
        CreatePetV2Request request
    );

    @Operation(
        summary = "Search pets with pagination (V2)",
        description = "Public endpoint to search and filter pets available for adoption."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paginated list of pets",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid search criteria parameters",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    PageResponse<PetV2SummaryResponse> findPets(
        PetV2SearchCriteriaRequest criteria,
        Pageable pageable
    );

    @Operation(
        summary = "Get pet details by ID (V2)",
        description = "Public endpoint to retrieve complete details and images of a pet."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pet details found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PetV2Response.class))),
        @ApiResponse(responseCode = "404", description = "Pet not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    PetV2Response findPetById(UUID id);

    @Operation(
        summary = "Update pet details (V2)",
        description = "Updates pet information. Requires pet guardian ownership or ADMIN role."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pet updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PetV2Response.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
            description = "Unauthorized - Invalid authentication credentials",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Not guardian owner or insufficient permissions",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Unprocessable Entity - Active shelter profile not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Pet not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    PetV2Response updatePet(
        UUID id,
        AuthenticatedUser authenticatedUser,
        UpdatePetV2Request request
    );
}
