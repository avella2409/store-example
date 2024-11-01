package com.avella.store.ProductInfo.domain;

import java.util.Optional;

public interface ProductRepository {

    void save(Product product);

    Optional<Product> product(ProductId productId);
}
