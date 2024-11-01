package com.avella.store.ProductInfo.unit;

import com.avella.shared.application.CommandHandler;
import com.avella.store.ProductInfo.application.command.CreateProductCommand;
import com.avella.store.ProductInfo.application.command.handler.CreateProductHandler;
import com.avella.store.ProductInfo.domain.Product;
import com.avella.store.ProductInfo.domain.ProductId;
import com.avella.store.ProductInfo.domain.shared.Entity;
import com.avella.store.ProductInfo.unit.impl.InMemoryProductRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateProductTest {

    private final InMemoryProductRepository productRepository = new InMemoryProductRepository();

    private final CommandHandler<CreateProductCommand> handler = new CreateProductHandler(productRepository);

    @Test
    void createProduct() {
        handler.handle(new CreateProductCommand("merchant1", "product1"));

        var snap = productRepository.productSnapshot(ProductId.of("merchant1", "product1")).get();

        assertEquals("", snap.name());
        assertEquals("", snap.description());
    }

    @Test
    void creatingProductIsIdempotent() {
        var id = ProductId.of("merchant1", "product1");
        var snapshot = new Product.Snapshot(
                new Entity.Snapshot<>(
                        id,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        0
                ),
                "Some name",
                "Some description"
        );
        productRepository.saveSnapshot(snapshot);

        handler.handle(new CreateProductCommand("merchant1", "product1"));

        assertEquals(snapshot, productRepository.productSnapshot(id).get());
    }
}
