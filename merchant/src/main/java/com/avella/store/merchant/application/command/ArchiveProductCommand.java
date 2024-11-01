package com.avella.store.merchant.application.command;

import com.avella.shared.application.Command;

public record ArchiveProductCommand(String merchantId, String productId) implements Command {
}