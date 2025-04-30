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
                .title("Microservice " + " - [" + buildProperties.getName() + "]")
                .description(
                    "Artifact: " + buildProperties.getArtifact()
                        + " | Timestamp of the Build: "
                        + buildProperties.getTime().atZone(ZoneId.systemDefault()))
                .version(buildProperties.getVersion())
                .license(getLicense()))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    private License getLicense() {
        return new License()
            .name(String.format("%s developed by hywenklis", buildProperties.getName()));
    }
}
