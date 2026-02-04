package com.erp.mini.util;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class TestAuditorAware implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.of(1L); // 테스트용 사용자 ID
    }
}
