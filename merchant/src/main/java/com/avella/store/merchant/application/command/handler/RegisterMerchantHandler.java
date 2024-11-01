package com.avella.store.merchant.application.command.handler;

import com.avella.shared.application.CommandHandler;
import com.avella.store.merchant.application.command.RegisterMerchantCommand;
import com.avella.store.merchant.domain.Merchant;
import com.avella.store.merchant.domain.MerchantRepository;

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
