package com.buddy.api.web.profiles.requests;

import com.buddy.api.domains.profile.enums.ProfileTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ProfileRequest(
    @Schema(description = "Profile account ID", example = "600c7ef2-5868-430e-8bf1-40407c635049")
    @NotNull(message = "Profile account ID is mandatory")
    UUID accountId,

    @Schema(description = "Profile name", example = "Buddy")
    @NotBlank(message = "Profile name is mandatory")
    @Size(message = "Profile name must have between 3 and 100 characters", min = 3, max = 100)
    String name,

    @Schema(description = "Profile description", example = "Pet lover")
    @Size(message = "Profile description must have at most 255 characters", max = 255)
    String description,

    @Schema(
        description = "Profile bio",
        example = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit.
            Integer augue nulla, porta eget nunc finibus, lacinia sagittis turpis.
            Suspendisse fringilla tempus blandit. Nulla ut risus nisl. Curabitur imperdiet dapibus
            orci, in aliquam risus fringilla vitae. Phasellus iaculis ornare libero euismod feugiat
            Quisque vestibulum quam mollis puruseleifend, nec condimentum massa fermentum.
            Phasellus at dui accumsan, volutpat magna ac, blandit ipsum."""
    )
    String bio,

    @Schema(description = "Profile type", example = "USER")
    @NotNull(message = "Profile type is mandatory")
    @Enumerated(EnumType.STRING)
    ProfileTypeEnum profileType
) {
}
