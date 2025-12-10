package com.buddy.api.commons.configurations.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.time.ZoneId;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";
    private final BuildProperties buildProperties;

    public SwaggerApiConfig(final BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI swaggerApi() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                    .name(BEARER_AUTH)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT Authorization header using the Bearer scheme.")))
            .info(new Info()
                .title("Buddy API")
                .description("API for managing adoptions, pets, and shelters. "
                    + "Artifact: " + buildProperties.getArtifact()
                    + " | Version: " + buildProperties.getVersion()
                    + " | Build Time: "
                    + buildProperties.getTime().atZone(ZoneId.systemDefault()))
                .version(buildProperties.getVersion())
                .contact(new io.swagger.v3.oas.models.info.Contact()
                    .name("Buddy API Team")
                    .email("contact@buddy.com")
                    .url("https://buddy.com"))
                .license(getLicense()))
            .externalDocs(new io.swagger.v3.oas.models.ExternalDocumentation()
                .description("Project Documentation & Source Code")
                .url("https://github.com/hywenklis/buddy-api"))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    private License getLicense() {
        return new License()
            .name("Licença GNU Affero General Public License v3.0 (Copyright 2024 Hywenklis)")
            .url("https://www.gnu.org/licenses/agpl-3.0.html");
    }
}
