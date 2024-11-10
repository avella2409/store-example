package com.avella.store.merchant.core.command.handler;

import com.avella.shared.application.CommandHandler;
import com.avella.store.merchant.core.command.RegisterMerchantCommand;
import com.avella.store.merchant.core.command.domain.Merchant;
import com.avella.store.merchant.core.command.domain.MerchantRepository;

// Idempotent
public class RegisterMerchantHandler implements CommandHandler<RegisterMerchantCommand> {

    private final MerchantRepository merchantRepository;

    public RegisterMerchantHandler(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Override
    public void handle(RegisterMerchantCommand registerMerchantCommand) {
        if (merchantRepository.merchant(registerMerchantCommand.merchantId()).isEmpty())
            merchantRepository.save(Merchant.register(registerMerchantCommand.merchantId()));
    }
}
