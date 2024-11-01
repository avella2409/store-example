package com.avella.store.merchant.infrastructure.repository;

import com.avella.store.merchant.infrastructure.repository.model.MerchantDb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMerchantRepository extends JpaRepository<MerchantDb, String> {
}
