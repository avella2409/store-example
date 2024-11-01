package com.avella.store.merchant.domain;

public interface PublishingRulesEngine {
    boolean canPublish(String merchantId, String productId, String publishingId);
}
