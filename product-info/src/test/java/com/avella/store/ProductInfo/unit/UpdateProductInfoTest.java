package com.avella.store.ProductInfo.unit;

import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.CommandHandler;
import com.avella.store.ProductInfo.application.command.UpdateProductInfoCommand;
import com.avella.store.ProductInfo.application.command.handler.UpdateProductInfoHandler;
import com.avella.store.ProductInfo.domain.Product;
import com.avella.store.ProductInfo.domain.ProductId;
import com.avella.store.ProductInfo.domain.shared.Entity;
import com.avella.store.ProductInfo.unit.impl.CustomTextSafetyService;
import com.avella.store.ProductInfo.unit.impl.InMemoryProductRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UpdateProductInfoTest {
    private final InMemoryProductRepository productRepository = new InMemoryProductRepository();
    private final CustomTextSafetyService textSafetyService = new CustomTextSafetyService();

    private final CommandHandler<UpdateProductInfoCommand> handler =
            new UpdateProductInfoHandler(productRepository, textSafetyService);

    @Test
    void errorWhenProductNotFound() {
        var err = assertThrows(ApplicationException.class, () -> handler.handle(new UpdateProductInfoCommand(
                "merchant1", "notFoundProduct", "name", "description")));

        assertEquals("Product not found", err.getMessage());
    }

    @Test
    void errorWhenNameNotSafe() {
        productExist("merchant1", "product1");
        textSafetyService.failOn("Bad name");

        var err = assertThrows(ApplicationException.class, () -> handler.handle(new UpdateProductInfoCommand(
                "merchant1", "product1", "Bad name", "description")));

        assertEquals("Name not safe", err.getMessage());
    }

    @Test
    void errorWhenDescriptionNotSafe() {
        productExist("merchant1", "product1");
        textSafetyService.failOn("Bad Description");

        var err = assertThrows(ApplicationException.class, () -> handler.handle(new UpdateProductInfoCommand(
                "merchant1", "product1", "name", "Bad Description")));

        assertEquals("Description not safe", err.getMessage());
    }

    @Test
    void updateProductInfo() {
        productExist("merchant1", "product1");

        handler.handle(new UpdateProductInfoCommand("merchant1", "product1", "name", "description"));

        var snap = productRepository.productSnapshot(ProductId.of("merchant1", "product1")).get();

        assertEquals("name", snap.name());
        assertEquals("description", snap.description());
    }

    void productExist(String merchantId, String productId) {
        productRepository.saveSnapshot(new Product.Snapshot(
                new Entity.Snapshot<>(
                        ProductId.of(merchantId, productId),
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        0
                ),
                "",
                ""
        ));
    }
}
