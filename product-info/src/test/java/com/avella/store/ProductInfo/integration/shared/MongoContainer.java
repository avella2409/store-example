package com.avella.store.ProductInfo.integration.shared;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class MongoContainer {

    // Same version used on MongoDB Atlas
    static final MongoDBContainer mongoContainer =
            new MongoDBContainer(DockerImageName.parse("mongo:7.0.14"));

    static {
        System.out.println("Starting MongoDB container...");
        mongoContainer.start();
        System.out.println("MongoDB container started");
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongoContainer.getConnectionString() + "/somedb");
    }
}
