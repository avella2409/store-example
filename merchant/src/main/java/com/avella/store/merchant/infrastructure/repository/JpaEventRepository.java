package com.avella.store.merchant.infrastructure.repository;

import com.avella.store.merchant.infrastructure.repository.model.EventDb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEventRepository extends JpaRepository<EventDb, Long> {
}
