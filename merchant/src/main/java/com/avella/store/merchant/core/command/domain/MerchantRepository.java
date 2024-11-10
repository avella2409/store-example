package com.avella.store.merchant.core.command.domain;

import java.util.Optional;

public interface MerchantRepository {

    Optional<Merchant> merchant(String id);

    void save(Merchant merchant);
}
