package com.avella.store.merchant.unit.impl;

import com.avella.store.merchant.core.command.domain.PublishingRulesEngine;

public class CustomPublishingRulesEngine implements PublishingRulesEngine {

    private boolean result = true;

    public void alwaysFailPublishing() {
        result = false;
    }

    @Override
    public boolean canPublish(String merchantId, String productId, String publishingId) {
        return result;
    }
}
