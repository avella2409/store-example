package com.avella.store.merchant.domain;

import com.avella.shared.domain.DomainException;
import com.avella.shared.domain.Entity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Merchant extends Entity {

    public static final int MAX_PRODUCT_LIMIT = 10;

    private final Map<String, Product> products;

    private Merchant(String merchantId) {
        super(merchantId);
        this.products = new HashMap<>();
    }

    public static Merchant register(String merchantId) {
        var merchant = new Merchant(merchantId);
        merchant.raiseDomainEvent(new Event.MerchantRegistered(merchantId));

        return merchant;
    }

    public void createProduct(String productId, LocalDateTime creationTime) {
        if (products.size() >= MAX_PRODUCT_LIMIT) throw new DomainException("Maximum product limit exceeded");
        else if (products.containsKey(productId)) throw new DomainException("Product already exist");
        else {
            products.put(productId, Product.createDraft(productId, creationTime));
            raiseDomainEvent(new Event.ProductCreated(merchantId(), productId));
        }
    }

    public void publishProduct(String publishingId, PublishingRulesEngine publishingRulesEngine,
                               String productId, LocalDateTime publishTime) {
        switch (product(productId)) {
            case Product.Draft draft -> {
                if (publishingRulesEngine.canPublish(merchantId(), productId, publishingId)) {
                    products.replace(productId, draft.publish(publishTime));
                    raiseDomainEvent(new Event.ProductPublished(merchantId(), productId, publishingId));
                } else throw new DomainException("Product not ready to be published");
            }
            default -> throw new DomainException("Can only publish a draft product");
        }
    }

    public void archiveProduct(String productId, LocalDateTime archiveTime) {
        switch (product(productId)) {
            case Product.Published published -> {
                products.replace(productId, published.archive(archiveTime));
                raiseDomainEvent(new Event.ProductArchived(merchantId(), productId));
            }
            default -> throw new DomainException("Can only archive a published product");
        }
    }

    private Product product(String productId) {
        return Optional.ofNullable(products.get(productId))
                .orElseThrow(() -> new DomainException("Product not found"));
    }

    private String merchantId() {
        return id();
    }

    //////////// Snapshot Logic //////////////////////
    private Merchant(Snapshot snapshot) {
        super(snapshot.entitySnapshot);
        this.products = snapshot.products.stream()
                .collect(Collectors.toMap(
                        Product::productId,
                        Function.identity()
                ));
    }

    public static Merchant restore(Snapshot snapshot) {
        return new Merchant(snapshot);
    }

    public Snapshot snapshot() {
        return new Snapshot(entitySnapshot(), products.values().stream().toList());
    }

    public record Snapshot(Entity.Snapshot entitySnapshot, List<Product> products) {
    }
}
