package com.avella.store.merchant.application.command.handler;

import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.CommandHandler;
import com.avella.store.merchant.application.command.CreateProductCommand;
import com.avella.store.merchant.application.service.TimeService;
import com.avella.store.merchant.domain.MerchantRepository;

public class CreateProductHandler implements CommandHandler<CreateProductCommand> {

    private final MerchantRepository merchantRepository;
    private final TimeService timeService;

    public CreateProductHandler(MerchantRepository merchantRepository, TimeService timeService) {
        this.merchantRepository = merchantRepository;
        this.timeService = timeService;
    }

    @Override
    public void handle(CreateProductCommand createProductCommand) {
        var merchant = merchantRepository.merchant(createProductCommand.merchantId())
                .orElseThrow(() -> new ApplicationException("Unknown merchant"));

        merchant.createProduct(createProductCommand.productId(), timeService.localDateTime());

        merchantRepository.save(merchant);
    }
}
