package com.avella.store.ProductInfo.integration;

import com.avella.store.ProductInfo.domain.Product;
import com.avella.store.ProductInfo.domain.ProductId;
import com.avella.store.ProductInfo.domain.shared.Entity;
import com.avella.store.ProductInfo.infrastructure.repository.MongoProductRepository;
import com.avella.store.ProductInfo.infrastructure.repository.model.ProductInfoDb;
import com.avella.store.ProductInfo.integration.shared.MongoContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataMongoTest
@Import(MongoProductRepository.class)
@Tag("integration")
public class MongoProductRepositoryTest extends MongoContainer {

    @Autowired
    MongoOperations ops;

    @Autowired
    private MongoProductRepository productRepository;

    @BeforeEach
    void setup() {
        ops.findAllAndRemove(new Query(), ProductInfoDb.class);
    }

    @Test
    void saveAndRetrieve() {
        ProductId productId = ProductId.of("merchant1", "product1");
        var snap = new Product.Snapshot(
                new Entity.Snapshot<>(
                        productId,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        0
                ),
                "name",
                "description"
        );

        productRepository.save(Product.restore(snap));

        var savedSnap = productRepository.product(productId).get().snapshot();
        assertEquals(snap.name(), savedSnap.name());
        assertEquals(snap.description(), savedSnap.description());
    }

    @Test
    void versionUpdatedCorrectly() {
        ProductId productId = ProductId.of("merchant1", "product1");
        productRepository.save(Product.restore(new Product.Snapshot(
                new Entity.Snapshot<>(
                        productId,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        0
                ),
                "name",
                "description"
        )));
        var savedSnap = productRepository.product(productId).get().snapshot();
        assertEquals(1, savedSnap.entitySnapshot().version());

        productRepository.save(Product.restore(withName("New name", savedSnap)));

        savedSnap = productRepository.product(productId).get().snapshot();
        assertEquals(2, savedSnap.entitySnapshot().version());
        assertEquals("New name", savedSnap.name());
    }

    @Test
    void errorOnOptimisticConcurrency() {
        ProductId productId = ProductId.of("merchant1", "product1");
        var originalSnap = new Product.Snapshot(
                new Entity.Snapshot<>(
                        productId,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        0
                ),
                "name",
                "description"
        );
        productRepository.save(Product.restore(originalSnap));
        var savedSnap = productRepository.product(productId).get().snapshot();

        productRepository.save(Product.restore(withName("update 1", savedSnap)));

        assertThrows(OptimisticLockingFailureException.class,
                () -> productRepository.save(Product.restore(withName("update 2", savedSnap))));
    }

    @Test
    void errorWhenProductAlreadyExist() {
        ProductId productId = ProductId.of("merchant1", "product1");
        var originalSnap = new Product.Snapshot(
                new Entity.Snapshot<>(
                        productId,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        0
                ),
                "name",
                "description"
        );
        productRepository.save(Product.restore(originalSnap));

        assertThrows(DuplicateKeyException.class,
                () -> productRepository.save(Product.restore(withName("New name", originalSnap))));
    }

    private Product.Snapshot withName(String newName, Product.Snapshot snap) {
        return new Product.Snapshot(
                snap.entitySnapshot(),
                newName,
                snap.description()
        );
    }
}
