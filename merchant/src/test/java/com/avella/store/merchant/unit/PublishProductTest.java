package com.avella.store.merchant.unit;

import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.CommandHandler;
import com.avella.shared.domain.DomainException;
import com.avella.shared.domain.Entity;
import com.avella.store.merchant.core.command.PublishProductCommand;
import com.avella.store.merchant.core.command.handler.PublishProductHandler;
import com.avella.store.merchant.core.command.domain.Event;
import com.avella.store.merchant.core.command.domain.Merchant;
import com.avella.store.merchant.core.command.domain.Product;
import com.avella.store.merchant.unit.impl.CustomPublishingRulesEngine;
import com.avella.store.merchant.unit.impl.InMemoryEventToDispatchRepository;
import com.avella.store.merchant.unit.impl.InMemoryMerchantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class PublishProductTest {

    private final InMemoryEventToDispatchRepository eventToDispatchRepository =
            new InMemoryEventToDispatchRepository();
    private final InMemoryMerchantRepository merchantRepository = new InMemoryMerchantRepository(eventToDispatchRepository);
    private final CustomPublishingRulesEngine publishingRulesEngine = new CustomPublishingRulesEngine();
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime yesterday = now.minusDays(1);
    private final CommandHandler<PublishProductCommand> handler =
            new PublishProductHandler(merchantRepository, () -> now, publishingRulesEngine);

    @Test
    void errorWhenMerchantNotFound() {
        var error = assertThrows(ApplicationException.class,
                () -> handler.handle(publishProduct("notFound", "product1", "publish1")));

        assertEquals("Unknown merchant", error.getMessage());
    }

    @Test
    void errorWhenProductNotFound() {
        merchantRepository.saveSnapshot(merchant("merchant1", List.of()));

        var error = assertThrows(DomainException.class, () -> handler.handle(publishProduct("merchant1", "notFound", "publish1")));

        assertEquals("Product not found", error.getMessage());
    }

    @Test
    void errorWhenProductNotReadyToBePublished() {
        merchantRepository.saveSnapshot(merchant("merchant1",
                List.of(
                        new Product.Draft("product1", yesterday),
                        new Product.Draft("product2", yesterday)
                )));
        publishingRulesEngine.alwaysFailPublishing();

        var error = assertThrows(DomainException.class, () -> handler.handle(publishProduct("merchant1", "product1", "publish1")));

        assertEquals("Product not ready to be published", error.getMessage());
        assertHasSameProducts(List.of(
                new Product.Draft("product1", yesterday),
                new Product.Draft("product2", yesterday)
        ), merchantRepository.merchantSnapshot("merchant1").products());
    }

    @Test
    void publishProduct() {
        merchantRepository.saveSnapshot(merchant("merchant1",
                List.of(
                        new Product.Draft("product1", yesterday),
                        new Product.Draft("product2", yesterday)
                )));

        handler.handle(publishProduct("merchant1", "product1", "publish1"));

        assertHasSameProducts(
                List.of(
                        new Product.Published("product1", yesterday, now),
                        new Product.Draft("product2", yesterday)
                ),
                merchantRepository.merchantSnapshot("merchant1").products());
        assertTrue(eventToDispatchRepository.dispatched()
                .contains(new Event.ProductPublished("merchant1", "product1", "publish1")));
    }

    @ParameterizedTest
    @MethodSource("productStream")
    void errorWhenProductIsNotInDraft(Product product) {
        merchantRepository.saveSnapshot(merchant("merchant1", List.of(product)));

        var error = assertThrows(DomainException.class, () -> handler.handle(publishProduct("merchant1", product.productId(), "publish1")));

        assertEquals("Can only publish a draft product", error.getMessage());
    }

    private static Stream<Product> productStream() {
        return Stream.of(
                new Product.Published("published", LocalDateTime.MIN, LocalDateTime.MIN),
                new Product.Archived("archived", LocalDateTime.MIN, LocalDateTime.MIN)
        );
    }

    private Merchant.Snapshot merchant(String merchantId, List<Product> products) {
        return new Merchant.Snapshot(
                new Entity.Snapshot(merchantId, LocalDateTime.MIN, LocalDateTime.MIN, 1, null),
                products
        );
    }

    private PublishProductCommand publishProduct(String merchantId, String productId, String publishingId) {
        return new PublishProductCommand(merchantId, productId, publishingId);
    }

    private void assertHasSameProducts(List<Product> expected, List<Product> products) {
        assertEquals(expected.size(), products.size());
        assertTrue(products.containsAll(expected));
    }
}
