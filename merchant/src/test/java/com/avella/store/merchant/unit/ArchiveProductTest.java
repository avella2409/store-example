package com.avella.store.merchant.unit;

import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.CommandHandler;
import com.avella.shared.domain.DomainException;
import com.avella.shared.domain.Entity;
import com.avella.store.merchant.application.command.ArchiveProductCommand;
import com.avella.store.merchant.application.command.handler.ArchiveProductHandler;
import com.avella.store.merchant.domain.Event;
import com.avella.store.merchant.domain.Merchant;
import com.avella.store.merchant.domain.Product;
import com.avella.store.merchant.unit.impl.InMemoryEventToDispatchRepository;
import com.avella.store.merchant.unit.impl.InMemoryMerchantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ArchiveProductTest {

    private final InMemoryEventToDispatchRepository eventToDispatchRepository =
            new InMemoryEventToDispatchRepository();
    private final InMemoryMerchantRepository merchantRepository = new InMemoryMerchantRepository(eventToDispatchRepository);
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime yesterday = now.minusDays(1);
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
        merchantRepository.saveSnapshot(merchant("merchant1", List.of()));

        var error = assertThrows(DomainException.class, () -> handler.handle(archiveProduct("merchant1", "notFound")));

        assertEquals("Product not found", error.getMessage());
    }

    @Test
    void archiveProduct() {
        merchantRepository.saveSnapshot(merchant("merchant1",
                List.of(
                        new Product.Published("product1", yesterday, yesterday),
                        new Product.Published("product2", yesterday, yesterday)
                )));

        handler.handle(archiveProduct("merchant1", "product1"));

        assertHasSameProducts(List.of(
                new Product.Archived("product1", yesterday, now),
                new Product.Published("product2", yesterday, yesterday)
        ), merchantRepository.merchantSnapshot("merchant1").products());

        assertTrue(eventToDispatchRepository.dispatched()
                .contains(new Event.ProductArchived("merchant1", "product1")));
    }

    @ParameterizedTest
    @MethodSource("productNotPublishedStream")
    void errorWhenProductIsNotPublished(Product product) {
        merchantRepository.saveSnapshot(merchant("merchant1", List.of(product)));

        var error = assertThrows(DomainException.class, () -> handler.handle(archiveProduct("merchant1", product.productId())));

        assertEquals("Can only archive a published product", error.getMessage());
    }

    private static Stream<Product> productNotPublishedStream() {
        return Stream.of(
                new Product.Draft("draft", LocalDateTime.MIN),
                new Product.Archived("archived", LocalDateTime.MIN, LocalDateTime.MIN)
        );
    }

    private Merchant.Snapshot merchant(String merchantId, List<Product> products) {
        return new Merchant.Snapshot(
                new Entity.Snapshot(merchantId, LocalDateTime.MIN, LocalDateTime.MIN, 1, null),
                products
        );
    }

    private ArchiveProductCommand archiveProduct(String merchantId, String productId) {
        return new ArchiveProductCommand(merchantId, productId);
    }

    private void assertHasSameProducts(List<Product> expected, List<Product> products) {
        assertEquals(expected.size(), products.size());
        assertTrue(products.containsAll(expected));
    }
}
