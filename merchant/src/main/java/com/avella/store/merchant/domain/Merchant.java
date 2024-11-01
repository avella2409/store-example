package com.avella.store.merchant.domain;

import com.avella.shared.domain.DomainException;
import com.avella.shared.domain.Entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Merchant extends Entity {

    public static final int MAX_PRODUCT_LIMIT = 10;

    private final Set<Product> products;

    private Merchant(String merchantId) {
        super(merchantId);
        this.products = new HashSet<>();
        raiseDomainEvent(new Event.MerchantRegistered(merchantId));
    }

    public static Merchant register(String merchantId) {
        return new Merchant(merchantId);
    }

    public void createProduct(String productId, LocalDateTime creationTime) {
        if (products.size() >= MAX_PRODUCT_LIMIT) throw new DomainException("Maximum product limit exceeded");
        else if (products.stream().anyMatch(product -> product.productId().equals(productId)))
            throw new DomainException("Product Id already used");
        else {
            products.add(Product.createDraft(productId, creationTime));
            raiseDomainEvent(new Event.ProductCreated(merchantId(), productId));
        }
    }

    public void publishProduct(String publishingId,
                               PublishingRulesEngine publishingRulesEngine,
                               String productId, LocalDateTime publishTime) {
        switch (product(productId)) {
            case Product.Draft draft -> {
                if (publishingRulesEngine.canPublish(merchantId(), productId, publishingId)) {
                    updateProduct(draft, draft.publish(publishTime));
                    raiseDomainEvent(new Event.ProductPublished(merchantId(), productId, publishingId));
                } else throw new DomainException("Product not ready to be published");
            }
            default -> throw new DomainException("Can only publish a draft product");
        }
    }

    public void archiveProduct(String productId, LocalDateTime archiveTime) {
        switch (product(productId)) {
            case Product.Published published -> {
                updateProduct(published, published.archive(archiveTime));
                raiseDomainEvent(new Event.ProductArchived(merchantId(), productId));
            }
            default -> throw new DomainException("Can only archive a published product");
        }
    }

    private void updateProduct(Product from, Product to) {
        products.remove(from);
        products.add(to);
    }

    private Product product(String productId) {
        return products.stream()
                .filter(p -> p.productId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Product not found"));
    }

    private String merchantId() {
        return id();
    }

    //////////// Snapshot Logic //////////////////////
    private Merchant(Snapshot snapshot) {
        super(snapshot.entitySnapshot);
        this.products = new HashSet<>(snapshot.products);
    }

    public static Merchant restore(Snapshot snapshot) {
        return new Merchant(snapshot);
    }

    public Snapshot snapshot() {
        return new Snapshot(entitySnapshot(), products);
    }

    public record Snapshot(Entity.Snapshot entitySnapshot, Set<Product> products) {
    }
}
