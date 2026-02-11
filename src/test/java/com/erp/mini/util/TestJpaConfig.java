package com.erp.mini.util;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;

@TestConfiguration
public class TestJpaConfig {

    @Bean
    @Primary
    AuditorAware<Long> auditorAware() {
        return new TestAuditorAware();
    }
}