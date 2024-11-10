package com.avella.store.merchant.core.command.domain;

public interface PublishingRulesEngine {
    boolean canPublish(String merchantId, String productId, String publishingId);
}
