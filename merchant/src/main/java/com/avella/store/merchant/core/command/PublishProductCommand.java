package com.avella.store.merchant.core.command;

import com.avella.shared.application.Command;

public record PublishProductCommand(String merchantId, String productId, String publishingId) implements Command {
}
