package com.avella.store.ProductInfo.application.command;

import com.avella.shared.application.Command;

// Idempotent
public record CreateProductCommand(String merchantId, String productId) implements Command {
}
