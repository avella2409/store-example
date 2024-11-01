package com.avella.store.service;

import com.avella.store.shared.ProductStatus;

import java.util.List;

public interface MerchantService {
    String createProduct(String accessToken);

    void publishProduct(String accessToken, String productId);

    List<ProductStatus> findAllProductStatus(String accessToken);
}
