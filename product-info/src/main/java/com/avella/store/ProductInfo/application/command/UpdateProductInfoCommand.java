package com.avella.store.ProductInfo.application.command;

import com.avella.shared.application.Command;

public record UpdateProductInfoCommand(String merchantId, String productId,
                                       String name, String description) implements Command {
}
