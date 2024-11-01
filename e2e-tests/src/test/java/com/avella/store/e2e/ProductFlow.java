package com.avella.store.e2e;

import com.avella.store.Merchant;
import com.avella.store.MerchantFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProductFlow {

    private final Merchant merchant = MerchantFactory.create();

    @BeforeEach
    void setup() {
        System.out.println(Thread.currentThread().getName()); // Run tests concurrently
    }

    @AfterEach
    void tearDown() {
        merchant.deleteAllResources();
    }

    @Test
    void publishProduct() {
        merchant.register();
        merchant.login();

        String productId = merchant.createProduct();

        merchant.fillProductInfo(productId, "Product name", "Product description");

        merchant.publishProduct(productId);

        assertEquals("PUBLISHED", merchant.productStatus(productId));
    }

    @Test
    void cannotPublishWhenProductInfoNotFilled() {
        merchant.register();
        merchant.login();

        String productId = merchant.createProduct();

        assertThrows(RuntimeException.class, () -> merchant.publishProduct(productId));
    }

    @Test
    void cannotPublishAProductTwice() {
        merchant.register();
        merchant.login();

        String productId = merchant.createProduct();

        merchant.fillProductInfo(productId, "Product name", "Product description");

        merchant.publishProduct(productId);
        assertThrows(RuntimeException.class, () -> merchant.publishProduct(productId));
    }

    @Test
    void cannotPublishAProductWithEmptyName() {
        merchant.register();
        merchant.login();

        String productId = merchant.createProduct();

        merchant.fillProductInfo(productId, "", "Product description");

        assertThrows(RuntimeException.class, () -> merchant.publishProduct(productId));
    }

    @Test
    void cannotPublishAProductWithEmptyDescription() {
        merchant.register();
        merchant.login();

        String productId = merchant.createProduct();

        merchant.fillProductInfo(productId, "Product name", "");

        assertThrows(RuntimeException.class, () -> merchant.publishProduct(productId));
    }

    @Test
    void shouldBeLoggedInToCreateProduct() {
        merchant.register();

        assertThrows(RuntimeException.class, merchant::createProduct);
    }
}
