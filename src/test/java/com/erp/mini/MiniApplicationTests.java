package com.erp.mini;

import com.erp.mini.util.TestContainerManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Tag("integration")
@ActiveProfiles("integration")
@SpringBootTest
class MiniApplicationTests {

    @Test
    void contextLoads() {
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.startRedis();

        TestContainerManager.registerMySQL(registry);
        TestContainerManager.registerRedis(registry);
    }
}
