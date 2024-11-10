package com.avella.store.merchant.core.command.handler;

import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.CommandHandler;
import com.avella.store.merchant.core.command.ArchiveProductCommand;
import com.avella.store.merchant.core.command.domain.MerchantRepository;

public class ArchiveProductHandler implements CommandHandler<ArchiveProductCommand> {

    private final MerchantRepository merchantRepository;
    private final TimeService timeService;

    public ArchiveProductHandler(MerchantRepository merchantRepository,
                                 TimeService timeService) {
        this.merchantRepository = merchantRepository;
        this.timeService = timeService;
    }

    @Override
    public void handle(ArchiveProductCommand archiveProductCommand) {
        var merchant = merchantRepository.merchant(archiveProductCommand.merchantId())
                .orElseThrow(() -> new ApplicationException("Unknown merchant"));

        merchant.archiveProduct(archiveProductCommand.productId(), timeService.localDateTime());

        merchantRepository.save(merchant);
    }
}
