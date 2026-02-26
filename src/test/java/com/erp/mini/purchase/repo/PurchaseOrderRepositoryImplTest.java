package com.erp.mini.purchase.repo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.erp.mini.common.config.JpaConfig;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.purchase.domain.PurchaseOrder;
import com.erp.mini.purchase.domain.PurchaseStatus;
import com.erp.mini.purchase.dto.PurchaseHeaderDto;
import com.erp.mini.purchase.dto.PurchaseLineDto;
import com.erp.mini.util.TestAuditorConfig;
import com.erp.mini.util.TestContainerManager;
import com.erp.mini.util.TestQuerydslConfig;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ PurchaseOrderRepositoryImpl.class, JpaConfig.class, TestAuditorConfig.class, TestQuerydslConfig.class })
@ActiveProfiles("integration")
class PurchaseOrderRepositoryImplTest {
        @Autowired
        private PurchaseOrderRepository repository;

        private Long id;
        private Partner partner;
        private Item item1;
        private Item item2;
        private Warehouse warehouse;

        @PersistenceContext
        private EntityManager em;

        @DynamicPropertySource
        static void properties(DynamicPropertyRegistry registry) {
                TestContainerManager.registerMySQL(registry);
        }

        private Partner createPartner() {
                Partner p = Partner.createPartner("SUP", PartnerType.SUPPLIER, "010-0000-0000", "sup@company.com");
                em.persist(p);

                return p;
        }

        private Item createItem(String name, String code) {
                Item it = Item.createItem(name, code, BigDecimal.valueOf(1000), ItemStatus.ACTIVE);
                em.persist(it);
                return it;
        }

        private Warehouse createWarehouse(String name) {
                Warehouse w = Warehouse.createWarehouse(name, "loc", WarehouseStatus.ACTIVE);
                em.persist(w);
                return w;
        }

        @BeforeEach
        void setUp() {
                partner = createPartner();
                item1 = createItem("ITEM-1", "ITM-1");
                item2 = createItem("ITEM-2", "ITM-2");
                warehouse = createWarehouse("WH-1");

                PurchaseOrder purchaseOrder = PurchaseOrder.createPurchaseOrder(partner);
                purchaseOrder.addLine(item1, warehouse, 10, BigDecimal.valueOf(2000));
                purchaseOrder.addLine(item2, warehouse, 10, BigDecimal.valueOf(3000));

                id = repository.save(purchaseOrder).getId();

                em.flush();
                em.clear();
        }

        @Test
        void get_purchase_header_test() {
                PurchaseHeaderDto header = repository.getPurchaseDetailHeader(id);

                assertThat(header).isNotNull();
                assertThat(header.partnerName()).isEqualTo(partner.getName());
                assertThat(header.partnerCode()).isEqualTo(partner.getCode());
                assertThat(header.status()).isEqualTo(PurchaseStatus.CREATED);
        }

        @Test
        void get_purchase_lines_test() {
                List<PurchaseLineDto> lines = repository.getPurchaseDetailLines(id);

                assertThat(lines).isNotNull();
                assertThat(lines.size()).isEqualTo(2);
                assertThat(lines)
                                .extracting(
                                                PurchaseLineDto::itemCode,
                                                PurchaseLineDto::qty,
                                                PurchaseLineDto::unitCost)
                                .containsExactlyInAnyOrder(
                                                tuple(item1.getCode(), 10L, new BigDecimal("2000.00")),
                                                tuple(item2.getCode(), 10L, new BigDecimal("3000.00")));
        }
}