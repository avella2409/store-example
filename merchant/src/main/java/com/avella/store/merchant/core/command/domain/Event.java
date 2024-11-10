package com.avella.store.merchant.core.command.domain;

import com.avella.shared.domain.DomainEvent;

public sealed interface Event extends DomainEvent {

    record MerchantRegistered(String merchantId) implements Event {
    }

    record ProductCreated(String merchantId, String productId) implements Event {
    }

    record ProductPublished(String merchantId, String productId, String publishingId) implements Event {
    }

    record ProductArchived(String merchantId, String productId) implements Event {
    }
}
