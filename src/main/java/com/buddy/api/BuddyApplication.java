package com.buddy.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@ConfigurationPropertiesScan(basePackages = "com.buddy.api.commons.configurations.properties")
public class BuddyApplication {

    public static void main(final String[] args) {
        SpringApplication.run(BuddyApplication.class, args);
    }
}
