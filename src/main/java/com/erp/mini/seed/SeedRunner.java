package com.erp.mini.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 시드 데이터 추가 (로컬 테스트용)
 * ApplicationRunner => 모든 bean이 등록된 후 실행됨
 */

@Profile("local")
@Component
@RequiredArgsConstructor
public class SeedRunner implements ApplicationRunner {

    private final SeedService seedService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        seedService.seed();
    }
}