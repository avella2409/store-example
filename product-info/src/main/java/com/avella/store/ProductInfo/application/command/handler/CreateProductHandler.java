package com.avella.store.ProductInfo.application.command.handler;


import com.avella.shared.application.CommandHandler;
import com.avella.store.ProductInfo.application.command.CreateProductCommand;
import com.avella.store.ProductInfo.domain.Product;
import com.avella.store.ProductInfo.domain.ProductId;
import com.avella.store.ProductInfo.domain.ProductRepository;

// Idempotent
public class CreateProductHandler implements CommandHandler<CreateProductCommand> {

    private final ProductRepository productRepository;

    public CreateProductHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void handle(CreateProductCommand createProductCommand) {
        var productId = ProductId.of(createProductCommand.merchantId(), createProductCommand.productId());
        if (productRepository.product(productId).isEmpty())
            productRepository.save(Product.create(productId));
    }
}
