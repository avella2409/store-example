package com.avella.store.merchant.unit.impl;

import com.avella.store.merchant.core.command.domain.Event;
import com.avella.store.merchant.core.command.domain.Merchant;
import com.avella.store.merchant.core.command.domain.MerchantRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryMerchantRepository implements MerchantRepository {

    private final InMemoryEventToDispatchRepository eventToDispatchRepository;
    private final Map<String, Merchant.Snapshot> merchants = new HashMap<>();

    public InMemoryMerchantRepository(InMemoryEventToDispatchRepository eventToDispatchRepository) {
        this.eventToDispatchRepository = eventToDispatchRepository;
    }

    @Override
    public Optional<Merchant> merchant(String id) {
        return merchants.containsKey(id) ? Optional.of(Merchant.restore(merchants.get(id))) : Optional.empty();
    }

    @Override
    public void save(Merchant merchant) {
        var snapshot = merchant.snapshot();
        saveSnapshot(snapshot);
        snapshot.entitySnapshot().eventsToDispatch().forEach(event ->
                eventToDispatchRepository.save((Event) event));
    }

    public void saveSnapshot(Merchant.Snapshot snapshot) {
        merchants.put(snapshot.entitySnapshot().id(), snapshot);
    }

    public Merchant.Snapshot merchantSnapshot(String id) {
        return merchant(id).get().snapshot();
    }
}
