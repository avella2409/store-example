package com.avella.store.merchant.core.command;

import com.avella.shared.application.Command;

public record ArchiveProductCommand(String merchantId, String productId) implements Command {
}
