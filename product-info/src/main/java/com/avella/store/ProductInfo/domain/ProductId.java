package com.avella.store.ProductInfo.domain;

public record ProductId(String merchantId, String productId) {

    public static ProductId of(String merchantId, String productId) {
        return new ProductId(merchantId, productId);
    }
}
