package com.avella.store.merchant.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfiguration {
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> customMeterRegistry(@Value("${spring.application.name}") String appName) {
        return registry -> registry.config().commonTags("app", appName);
    }
}
