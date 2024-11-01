package com.avella.store;

public interface Merchant {

    void register();

    void login();

    String createProduct();

    void fillProductInfo(String productId, String productName, String productDescription);

    void publishProduct(String productId);

    String productStatus(String productId);

    void deleteAllResources();
}
