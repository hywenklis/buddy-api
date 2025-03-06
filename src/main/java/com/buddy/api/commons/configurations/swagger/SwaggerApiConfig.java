package com.buddy.api.commons.configurations.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import java.time.ZoneId;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerApiConfig {

    private final BuildProperties buildProperties;

    public SwaggerApiConfig(final BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI swaggerApi() {
        return new OpenAPI()
            .components(new Components())
            .info(new Info()
                .title("Microservice " + " - [" + buildProperties.getName() + "]")
                .description(
                    "Artifact: " + buildProperties.getArtifact()
                        + " | Timestamp of the Build: "
                        + buildProperties.getTime().atZone(ZoneId.systemDefault()))
                .version(buildProperties.getVersion())
                .license(getLicense()));
    }

    private License getLicense() {
        return new License()
            .name(String.format("%s developed by hywenklis", buildProperties.getName()));
    }
}
