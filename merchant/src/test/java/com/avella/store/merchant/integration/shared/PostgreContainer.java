package com.avella.store.merchant.integration.shared;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class PostgreContainer {

    static final PostgreSQLContainer<?> postgreContainer = new PostgreSQLContainer<>("postgres");

    static {
        System.out.println("Starting Postgres container...");
        postgreContainer.start();
        System.out.println("Postgres container started");
    }

    @DynamicPropertySource
    static void postgreProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreContainer::getUsername);
        registry.add("spring.datasource.password", postgreContainer::getPassword);
    }
}
