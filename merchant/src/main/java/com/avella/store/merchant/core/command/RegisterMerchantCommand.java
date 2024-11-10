package com.avella.store.merchant.core.command;

import com.avella.shared.application.Command;

// Idempotent
public record RegisterMerchantCommand(String merchantId) implements Command {
}
