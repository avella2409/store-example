package com.avella.store.merchant.core.command.handler;

import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.CommandHandler;
import com.avella.store.merchant.core.command.PublishProductCommand;
import com.avella.store.merchant.core.command.domain.MerchantRepository;
import com.avella.store.merchant.core.command.domain.PublishingRulesEngine;

public class PublishProductHandler implements CommandHandler<PublishProductCommand> {

    private final MerchantRepository merchantRepository;
    private final TimeService timeService;
    private final PublishingRulesEngine publishingRulesEngine;

    public PublishProductHandler(MerchantRepository merchantRepository,
                                 TimeService timeService,
                                 PublishingRulesEngine publishingRulesEngine) {
        this.merchantRepository = merchantRepository;
        this.timeService = timeService;
        this.publishingRulesEngine = publishingRulesEngine;
    }

    @Override
    public void handle(PublishProductCommand publishProductCommand) {
        var merchant = merchantRepository.merchant(publishProductCommand.merchantId())
                .orElseThrow(() -> new ApplicationException("Unknown merchant"));

        merchant.publishProduct(publishProductCommand.publishingId(), publishingRulesEngine,
                publishProductCommand.productId(), timeService.localDateTime());

        merchantRepository.save(merchant);
    }
}
