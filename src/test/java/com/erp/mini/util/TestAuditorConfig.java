package com.erp.mini.util;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@TestConfiguration
public class TestAuditorConfig {

    @Bean("auditorAware")
    public AuditorAware<Long> auditorAware() {
        return () -> Optional.of(1L);
    }
}
