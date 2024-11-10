package com.avella.store.merchant.infrastructure.service;

import com.avella.store.merchant.core.command.handler.TimeService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class DefaultTimeService implements TimeService {
    @Override
    public LocalDateTime localDateTime() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
