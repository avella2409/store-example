package com.avella.store.merchant.unit;

import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.CommandHandler;
import com.avella.shared.domain.DomainException;
import com.avella.shared.domain.Entity;
import com.avella.store.merchant.application.command.CreateProductCommand;
import com.avella.store.merchant.application.command.handler.CreateProductHandler;
import com.avella.store.merchant.domain.Event;
import com.avella.store.merchant.domain.Merchant;
import com.avella.store.merchant.domain.Product;
import com.avella.store.merchant.unit.impl.InMemoryEventToDispatchRepository;
import com.avella.store.merchant.unit.impl.InMemoryMerchantRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class CreateProductTest {

    private final LocalDateTime now = LocalDateTime.now();
    private final InMemoryEventToDispatchRepository eventToDispatchRepository =
            new InMemoryEventToDispatchRepository();
    private final InMemoryMerchantRepository merchantRepository = new InMemoryMerchantRepository(eventToDispatchRepository);
    private final CommandHandler<CreateProductCommand> handler =
            new CreateProductHandler(merchantRepository, () -> now);

    @Test
    void errorWhenMerchantNotFound() {
        var error = assertThrows(ApplicationException.class, () -> handler.handle(createProduct("notFound", "product1")));

        assertEquals("Unknown merchant", error.getMessage());
    }

    @Test
    void createProduct() {
        merchantRepository.saveSnapshot(merchant("merchant1"));

        handler.handle(createProduct("merchant1", "product1"));

        assertHasSameProducts(
                List.of(new Product.Draft("product1", now)),
                merchantRepository.merchantSnapshot("merchant1").products()
        );
        assertTrue(eventToDispatchRepository.dispatched()
                .contains(new Event.ProductCreated("merchant1", "product1")));
    }

    @Test
    void errorWhenExceedingMaxProductLimit() {
        merchantRepository.saveSnapshot(merchant("merchant1"));

        IntStream.range(0, Merchant.MAX_PRODUCT_LIMIT).forEach(i -> handler.handle(createProduct("merchant1", "product" + i)));

        var error = assertThrows(DomainException.class, () -> handler.handle(createProduct("merchant1", "tooMuchProduct")));

        assertEquals("Maximum product limit exceeded", error.getMessage());
    }

    @Test
    void errorWhenProductIdIsAlreadyInUse() {
        merchantRepository.saveSnapshot(merchant("merchant1"));

        handler.handle(createProduct("merchant1", "product1"));

        var error = assertThrows(DomainException.class, () -> handler.handle(createProduct("merchant1", "product1")));

        assertEquals("Product already exist", error.getMessage());
    }

    private Merchant.Snapshot merchant(String merchantId) {
        return new Merchant.Snapshot(
                new Entity.Snapshot(merchantId, now, now, 1, null),
                List.of()
        );
    }

    private CreateProductCommand createProduct(String merchantId, String productId) {
        return new CreateProductCommand(merchantId, productId);
    }

    private void assertHasSameProducts(List<Product> expected, List<Product> products) {
        assertEquals(expected.size(), products.size());
        assertTrue(products.containsAll(expected));
    }
}
