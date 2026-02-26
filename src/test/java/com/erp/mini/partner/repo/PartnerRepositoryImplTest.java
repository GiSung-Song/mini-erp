package com.erp.mini.partner.repo;

import com.erp.mini.common.config.JpaConfig;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.partner.dto.SearchPartnerCondition;
import com.erp.mini.partner.dto.SearchPartnerResponse;
import com.erp.mini.util.TestAuditorConfig;
import com.erp.mini.util.TestContainerManager;
import com.erp.mini.util.TestQuerydslConfig;
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

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PartnerRepositoryImpl.class, JpaConfig.class, TestAuditorConfig.class, TestQuerydslConfig.class})
@ActiveProfiles("integration")
class PartnerRepositoryImplTest {

    @Autowired
    private PartnerRepository partnerRepository;

    @PersistenceContext
    private EntityManager em;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    @BeforeEach
    void setUp() {
        for (int i = 0; i < 10; i++) {
            Partner partner = Partner.createPartner(
                    "식품" + i,
                    i % 2 == 0 ? PartnerType.CUSTOMER : PartnerType.SUPPLIER,
                    null,
                    null
            );

            partnerRepository.save(partner);
            partner.generateCode();
        }

        em.flush();
        em.clear();
    }

    @Test
    void search_partners_test() {
        Pageable pageable = PageRequest.of(0, 5);

        SearchPartnerCondition condition = new SearchPartnerCondition(
                "식품", null
        );

        Page<SearchPartnerResponse> contents = partnerRepository.search(condition, pageable);

        Assertions.assertThat(contents).hasSize(5);
        Assertions.assertThat(contents.getTotalElements()).isEqualTo(10);

        SearchPartnerCondition condition2 = new SearchPartnerCondition(
                "식품", PartnerType.CUSTOMER
        );

        Page<SearchPartnerResponse> contents2 = partnerRepository.search(condition2, pageable);

        Assertions.assertThat(contents2).hasSize(5);
        Assertions.assertThat(contents2.getTotalElements()).isEqualTo(5);
    }
}