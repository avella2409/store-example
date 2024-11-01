package com.avella.store.merchant.unit;

import com.avella.store.merchant.unit.impl.InMemoryEventToDispatchRepository;
import com.avella.store.merchant.unit.impl.InMemoryMerchantRepository;
import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.CommandHandler;
import com.avella.shared.domain.DomainException;
import com.avella.shared.domain.Entity;
import com.avella.store.merchant.application.command.ArchiveProductCommand;
import com.avella.store.merchant.application.command.handler.ArchiveProductHandler;
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

public class ArchiveProductTest {

    private final LocalDateTime now = LocalDateTime.now();
    private final InMemoryEventToDispatchRepository eventToDispatchRepository =
            new InMemoryEventToDispatchRepository();
    private final InMemoryMerchantRepository merchantRepository = new InMemoryMerchantRepository(eventToDispatchRepository);
    private final CommandHandler<ArchiveProductCommand> handler =
            new ArchiveProductHandler(merchantRepository, () -> now);

    @Test
    void errorWhenMerchantNotFound() {
        var error = assertThrows(ApplicationException.class,
                () -> handler.handle(archiveProduct("notFound", "product1")));

        assertEquals("Unknown merchant", error.getMessage());
    }

    @Test
    void errorWhenProductNotFound() {
        merchantRepository.saveSnapshot(snapshot("merchant", Set.of()));

        var error = assertThrows(DomainException.class, () -> handler.handle(archiveProduct("merchant", "notFound")));

        assertEquals("Product not found", error.getMessage());
    }

    @Test
    void archiveProduct() {
        merchantRepository.saveSnapshot(snapshot("merchant",
                Set.of(
                        new Product.Published("product1", now.minusDays(1), now.minusSeconds(1)),
                        new Product.Published("product2", now.minusDays(1), now.minusSeconds(1))
                )));

        handler.handle(archiveProduct("merchant", "product1"));

        assertEquals(Set.of(
                        new Product.Archived("product1", now.minusDays(1), now),
                        new Product.Published("product2", now.minusDays(1), now.minusSeconds(1))
                ),
                merchantRepository.merchantSnapshot("merchant").products());

        assertTrue(eventToDispatchRepository.dispatched()
                .contains(new Event.ProductArchived("merchant", "product1")));
    }

    @ParameterizedTest
    @MethodSource("productNotPublishedStream")
    void errorWhenProductIsNotPublished(Product product) {
        merchantRepository.saveSnapshot(snapshot("merchant", Set.of(product)));

        var error = assertThrows(DomainException.class, () -> handler.handle(archiveProduct("merchant", product.productId())));

        assertEquals("Can only archive a published product", error.getMessage());
    }

    private static Stream<Product> productNotPublishedStream() {
        return Stream.of(
                new Product.Draft("draft", LocalDateTime.MIN),
                new Product.Archived("archived", LocalDateTime.MIN, LocalDateTime.MIN)
        );
    }

    private Merchant.Snapshot snapshot(String merchantId, Set<Product> products) {
        return new Merchant.Snapshot(
                new Entity.Snapshot(merchantId, LocalDateTime.MIN, LocalDateTime.MIN, 1, null),
                products
        );
    }

    private ArchiveProductCommand archiveProduct(String merchantId, String productId) {
        return new ArchiveProductCommand(merchantId, productId);
    }
}
