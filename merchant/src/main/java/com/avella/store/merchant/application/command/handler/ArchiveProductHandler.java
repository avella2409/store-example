package com.avella.store.merchant.application.command.handler;

import com.avella.shared.application.ApplicationException;
import com.avella.shared.application.CommandHandler;
import com.avella.store.merchant.application.command.ArchiveProductCommand;
import com.avella.store.merchant.application.service.TimeService;
import com.avella.store.merchant.domain.MerchantRepository;
import jakarta.transaction.Transactional;

public class ArchiveProductHandler implements CommandHandler<ArchiveProductCommand> {

    private final MerchantRepository merchantRepository;
    private final TimeService timeService;

    public ArchiveProductHandler(MerchantRepository merchantRepository,
                                 TimeService timeService) {
        this.merchantRepository = merchantRepository;
        this.timeService = timeService;
    }

    @Override
    @Transactional
    public void handle(ArchiveProductCommand archiveProductCommand) {
        var merchant = merchantRepository.merchant(archiveProductCommand.merchantId())
                .orElseThrow(() -> new ApplicationException("Unknown merchant"));

        merchant.archiveProduct(archiveProductCommand.productId(), timeService.localDateTime());

        merchantRepository.save(merchant);
    }
}
