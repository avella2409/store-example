package com.avella.store.ProductInfo.unit.impl;

import com.avella.store.ProductInfo.domain.Product;
import com.avella.store.ProductInfo.domain.ProductId;
import com.avella.store.ProductInfo.domain.ProductRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryProductRepository implements ProductRepository {

    private final Map<ProductId, Product.Snapshot> db = new HashMap<>();

    @Override
    public void save(Product product) {
        saveSnapshot(product.snapshot());
    }

    public void saveSnapshot(Product.Snapshot snapshot) {
        db.put(snapshot.entitySnapshot().id(), snapshot);
    }

    @Override
    public Optional<Product> product(ProductId productId) {
        return productSnapshot(productId).map(Product::restore);
    }

    public Optional<Product.Snapshot> productSnapshot(ProductId productId) {
        return db.containsKey(productId) ? Optional.of(db.get(productId)) : Optional.empty();
    }
}
