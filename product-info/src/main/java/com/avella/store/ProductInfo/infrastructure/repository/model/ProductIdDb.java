package com.avella.store.ProductInfo.infrastructure.repository.model;

public class ProductIdDb {

    private String merchantId;
    private String productId;

    public ProductIdDb(String merchantId, String productId) {
        this.merchantId = merchantId;
        this.productId = productId;
    }

    public ProductIdDb() {
    }


    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "ProductIdDb{" +
                "merchantId='" + merchantId + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }
}
