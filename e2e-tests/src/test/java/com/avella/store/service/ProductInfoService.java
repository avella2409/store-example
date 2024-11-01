package com.avella.store.service;

public interface ProductInfoService {

    void update(String accessToken, String productId, String name, String description);
}
