package com.buddy.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaRepositories
@ConfigurationPropertiesScan(basePackages = "com.buddy.api.commons.configurations.properties")
@EnableCaching
@EnableFeignClients(basePackages = "com.buddy.api.integrations.clients")
@EnableAsync
public class BuddyApplication {

    public static void main(final String[] args) {
        SpringApplication.run(BuddyApplication.class, args);
    }
}
