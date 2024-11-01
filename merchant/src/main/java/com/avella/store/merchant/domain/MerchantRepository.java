package com.avella.store.merchant.domain;

import java.util.Optional;

public interface MerchantRepository {

    Optional<Merchant> merchant(String id);

    void save(Merchant merchant);
}
