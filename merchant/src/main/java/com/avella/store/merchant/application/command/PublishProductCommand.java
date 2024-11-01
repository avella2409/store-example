package com.avella.store.merchant.application.command;

import com.avella.shared.application.Command;

public record PublishProductCommand(String merchantId, String productId, String publishingId) implements Command {
}
