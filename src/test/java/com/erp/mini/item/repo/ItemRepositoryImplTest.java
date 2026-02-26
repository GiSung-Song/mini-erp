package com.erp.mini.item.repo;

import com.erp.mini.common.config.JpaConfig;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.item.dto.SearchItemCondition;
import com.erp.mini.item.dto.SearchItemResponse;
import com.erp.mini.util.TestAuditorConfig;
import com.erp.mini.util.TestContainerManager;
import com.erp.mini.util.TestQuerydslConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ItemRepositoryImpl.class, JpaConfig.class, TestAuditorConfig.class, TestQuerydslConfig.class})
@ActiveProfiles("integration")
class ItemRepositoryImplTest {

    @Autowired
    private ItemRepository itemRepository;

    @PersistenceContext
    private EntityManager em;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    @BeforeEach
    void setUp() {
        List<Item> items = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Item item = Item.createItem(
                    "name" + i,
                    "C" + i,
                    BigDecimal.valueOf(10000),
                    ItemStatus.ACTIVE
            );

            items.add(item);
        }

        itemRepository.saveAll(items);

        em.flush();
        em.clear();
    }

    @Test
    void search_test() {
        Pageable pageable = PageRequest.of(0, 10);

        SearchItemCondition searchItemCondition = new SearchItemCondition(
                null, null
        );
        Page<SearchItemResponse> page = itemRepository.search(searchItemCondition, pageable);

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(10);

        SearchItemCondition searchItemCondition2 = new SearchItemCondition(
                "ITEM", null
        );
        Page<SearchItemResponse> page2 = itemRepository.search(searchItemCondition2, pageable);

        assertThat(page2.getContent()).hasSize(0);
        assertThat(page2.getTotalElements()).isEqualTo(0);
    }
}