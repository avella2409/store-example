package com.avella.store.merchant.core.command.domain;

import java.time.LocalDateTime;

public sealed interface Product {

    static Product.Draft createDraft(String productId, LocalDateTime creationTime) {
        return new Draft(productId, creationTime);
    }

    record Draft(String productId, LocalDateTime creationTime) implements Product {
        public Product.Published publish(LocalDateTime publishTime) {
            return new Published(productId, creationTime, publishTime);
        }
    }

    record Published(String productId, LocalDateTime creationTime, LocalDateTime publishTime) implements Product {
        public Product.Archived archive(LocalDateTime archiveTime) {
            return new Product.Archived(productId, creationTime, archiveTime);
        }
    }

    record Archived(String productId, LocalDateTime creationTime, LocalDateTime archiveTime) implements Product {
    }

    String productId();

    LocalDateTime creationTime();
}