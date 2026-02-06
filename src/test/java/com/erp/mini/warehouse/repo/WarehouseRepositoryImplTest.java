package com.erp.mini.warehouse.repo;

import com.erp.mini.util.TestContainerManager;
import com.erp.mini.util.TestJpaConfig;
import com.erp.mini.util.TestQuerydslConfig;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseStatus;
import com.erp.mini.warehouse.dto.SearchWarehouseCondition;
import com.erp.mini.warehouse.dto.SearchWarehouseResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({WarehouseRepositoryImpl.class, TestJpaConfig.class, TestQuerydslConfig.class})
@ActiveProfiles("integration")
class WarehouseRepositoryImplTest {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @PersistenceContext
    private EntityManager em;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    @BeforeEach
    void setUp() {
        for (int i = 1; i < 11; i++) {
            Warehouse warehouse = Warehouse.createWarehouse(
                    "창고" + i,
                    "서울" + i + "동",
                    i % 2 == 0 ? WarehouseStatus.ACTIVE : WarehouseStatus.INACTIVE
            );

            warehouseRepository.save(warehouse);
            warehouse.generateCode();
        }

        em.flush();
        em.clear();
    }

    @Test
    void search_warehouse_test() {
        Pageable pageable = PageRequest.of(0, 10);

        SearchWarehouseCondition condition1 = new SearchWarehouseCondition("창고", null);

        Page<SearchWarehouseResponse> response1 = warehouseRepository.search(condition1, pageable);

        assertThat(response1.getContent()).hasSize(10);
        assertThat(response1.getTotalElements()).isEqualTo(10);

        SearchWarehouseCondition condition2 = new SearchWarehouseCondition("창고", WarehouseStatus.ACTIVE);

        Page<SearchWarehouseResponse> response2 = warehouseRepository.search(condition2, pageable);

        assertThat(response2.getContent()).hasSize(5);
        assertThat(response2.getTotalElements()).isEqualTo(5);
    }

}