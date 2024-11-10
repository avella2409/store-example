package com.avella.store.merchant.unit;

import com.avella.store.merchant.unit.impl.InMemoryEventToDispatchRepository;
import com.avella.store.merchant.unit.impl.InMemoryMerchantRepository;
import com.avella.shared.application.CommandHandler;
import com.avella.store.merchant.core.command.RegisterMerchantCommand;
import com.avella.store.merchant.core.command.handler.RegisterMerchantHandler;
import com.avella.store.merchant.core.command.domain.Event;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegisterMerchantTest {

    private final InMemoryEventToDispatchRepository eventToDispatchRepository =
            new InMemoryEventToDispatchRepository();
    private final InMemoryMerchantRepository merchantRepository =
            new InMemoryMerchantRepository(eventToDispatchRepository);
    private final CommandHandler<RegisterMerchantCommand> handler =
            new RegisterMerchantHandler(merchantRepository);

    @Test
    void registerMerchant() {
        handler.handle(new RegisterMerchantCommand("merchant1"));

        assertTrue(merchantRepository.merchantSnapshot("merchant1").products().isEmpty());
        assertTrue(eventToDispatchRepository.dispatched()
                .contains(new Event.MerchantRegistered("merchant1")));
    }
}
