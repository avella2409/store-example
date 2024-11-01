package com.avella.store.merchant.unit;

import com.avella.store.merchant.unit.impl.CustomPublishingRulesEngine;
import com.avella.store.merchant.unit.impl.InMemoryEventToDispatchRepository;
import com.avella.store.merchant.unit.impl.InMemoryMerchantRepository;
import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.CommandHandler;
import com.avella.shared.domain.DomainException;
import com.avella.shared.domain.Entity;
import com.avella.store.merchant.application.command.PublishProductCommand;
import com.avella.store.merchant.application.command.handler.PublishProductHandler;
import com.avella.store.merchant.domain.Event;
import com.avella.store.merchant.domain.Merchant;
import com.avella.store.merchant.domain.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class PublishProductTest {

    private final LocalDateTime now = LocalDateTime.now();
    private final InMemoryEventToDispatchRepository eventToDispatchRepository =
            new InMemoryEventToDispatchRepository();
    private final InMemoryMerchantRepository merchantRepository = new InMemoryMerchantRepository(eventToDispatchRepository);
    private final CustomPublishingRulesEngine publishingRulesEngine = new CustomPublishingRulesEngine();
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
        merchantRepository.saveSnapshot(snapshot("merchant", Set.of()));

        var error = assertThrows(DomainException.class, () -> handler.handle(publishProduct("merchant", "notFound", "publish1")));

        assertEquals("Product not found", error.getMessage());
    }

    @Test
    void errorWhenProductNotReadyToBePublished() {
        merchantRepository.saveSnapshot(snapshot("merchant",
                Set.of(
                        new Product.Draft("product1", now.minusDays(1)),
                        new Product.Draft("product2", now.minusDays(1))
                )));
        publishingRulesEngine.alwaysFailPublishing();

        var error = assertThrows(DomainException.class, () -> handler.handle(publishProduct("merchant", "product1", "publish1")));

        assertEquals("Product not ready to be published", error.getMessage());
        assertEquals(Set.of(
                new Product.Draft("product1", now.minusDays(1)),
                new Product.Draft("product2", now.minusDays(1))
        ), merchantRepository.merchantSnapshot("merchant").products());
    }

    @Test
    void publishProduct() {
        merchantRepository.saveSnapshot(snapshot("merchant",
                Set.of(
                        new Product.Draft("product1", now.minusDays(1)),
                        new Product.Draft("product2", now.minusDays(1))
                )));

        handler.handle(publishProduct("merchant", "product1", "publish1"));

        assertEquals(Set.of(
                        new Product.Published("product1", now.minusDays(1), now),
                        new Product.Draft("product2", now.minusDays(1))
                ),
                merchantRepository.merchantSnapshot("merchant").products());

        assertTrue(eventToDispatchRepository.dispatched()
                .contains(new Event.ProductPublished("merchant", "product1", "publish1")));
    }

    @ParameterizedTest
    @MethodSource("productStream")
    void errorWhenProductIsNotInDraft(Product product) {
        merchantRepository.saveSnapshot(snapshot("merchant", Set.of(product)));

        var error = assertThrows(DomainException.class, () -> handler.handle(publishProduct("merchant", product.productId(), "publish1")));

        assertEquals("Can only publish a draft product", error.getMessage());
    }

    private static Stream<Product> productStream() {
        return Stream.of(
                new Product.Published("published", LocalDateTime.MIN, LocalDateTime.MIN),
                new Product.Archived("archived", LocalDateTime.MIN, LocalDateTime.MIN)
        );
    }

    private Merchant.Snapshot snapshot(String merchantId, Set<Product> products) {
        return new Merchant.Snapshot(
                new Entity.Snapshot(merchantId, LocalDateTime.MIN, LocalDateTime.MIN, 1, null),
                products
        );
    }

    private PublishProductCommand publishProduct(String merchantId, String productId, String publishingId) {
        return new PublishProductCommand(merchantId, productId, publishingId);
    }

}
