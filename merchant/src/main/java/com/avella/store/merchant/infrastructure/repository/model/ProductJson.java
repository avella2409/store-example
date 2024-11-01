package com.avella.store.merchant.infrastructure.repository.model;

public record ProductJson(String productId, long creationTime, String status,
                          Long publishedTime, Long archiveTime) {
}
