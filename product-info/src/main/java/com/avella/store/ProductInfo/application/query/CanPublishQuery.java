package com.avella.store.ProductInfo.application.query;

import com.avella.shared.application.Query;

public record CanPublishQuery(String merchantId, String productId) implements Query<Boolean> {
}
