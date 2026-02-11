package com.erp.mini.purchase.repo;

import com.erp.mini.item.domain.Item;
import com.erp.mini.item.repo.ItemRepository;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.repo.PartnerRepository;
import com.erp.mini.purchase.domain.PurchaseOrder;
import com.erp.mini.purchase.domain.PurchaseStatus;
import com.erp.mini.purchase.dto.PurchaseHeaderDto;
import com.erp.mini.purchase.dto.PurchaseLineDto;
import com.erp.mini.util.TestContainerManager;
import com.erp.mini.util.TestJpaConfig;
import com.erp.mini.util.TestQuerydslConfig;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.repo.WarehouseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PurchaseOrderRepositoryImpl.class, TestJpaConfig.class, TestQuerydslConfig.class})
@ActiveProfiles("integration")
class PurchaseOrderRepositoryImplTest {

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

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

    @BeforeEach
    void setUp() {
        partner = partnerRepository.findById(1L)
                .orElseThrow();

        item1 = itemRepository.findById(1L)
                .orElseThrow();

        item2 = itemRepository.findById(2L)
                .orElseThrow();

        warehouse = warehouseRepository.findById(1L)
                .orElseThrow();

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
                        PurchaseLineDto::unitCost
                )
                .containsExactlyInAnyOrder(
                        tuple(item1.getCode(), 10L, new BigDecimal("2000.00")),
                        tuple(item2.getCode(), 10L, new BigDecimal("3000.00"))
                );
    }
}