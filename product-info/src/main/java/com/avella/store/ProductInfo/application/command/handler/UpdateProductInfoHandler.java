package com.avella.store.ProductInfo.application.command.handler;

import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.CommandHandler;
import com.avella.store.ProductInfo.application.command.UpdateProductInfoCommand;
import com.avella.store.ProductInfo.domain.ProductId;
import com.avella.store.ProductInfo.domain.ProductRepository;
import com.avella.store.ProductInfo.domain.TextSafetyService;

public class UpdateProductInfoHandler implements CommandHandler<UpdateProductInfoCommand> {

    private final ProductRepository productRepository;
    private final TextSafetyService textSafetyService;

    public UpdateProductInfoHandler(ProductRepository productRepository, TextSafetyService textSafetyService) {
        this.productRepository = productRepository;
        this.textSafetyService = textSafetyService;
    }

    @Override
    public void handle(UpdateProductInfoCommand updateProductInfoCommand) {
        var product = productRepository.product(ProductId.of(updateProductInfoCommand.merchantId(), updateProductInfoCommand.productId()))
                .orElseThrow(() -> new ApplicationException("Product not found"));

        product.updateName(
                textSafetyService.verify(updateProductInfoCommand.name())
                        .orElseThrow(() -> new ApplicationException("Name not safe"))
        );
        product.updateDescription(
                textSafetyService.verify(updateProductInfoCommand.description())
                        .orElseThrow(() -> new ApplicationException("Description not safe"))
        );

        productRepository.save(product);
    }
}
