package com.erp.mini.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.erp.mini.common.response.PageResponse;
import com.erp.mini.inventory.domain.InventoryTransaction;
import com.erp.mini.inventory.domain.RefType;
import com.erp.mini.inventory.domain.TransactionType;
import com.erp.mini.inventory.dto.ItxDetailResponse;
import com.erp.mini.inventory.dto.ItxSearchCondition;
import com.erp.mini.inventory.dto.ItxSearchDto;
import com.erp.mini.inventory.repo.InventoryTransactionRepository;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.item.repo.ItemRepository;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.partner.repo.PartnerRepository;
import com.erp.mini.sales.domain.OrderCustomerInfo;
import com.erp.mini.sales.domain.SalesOrder;
import com.erp.mini.sales.domain.ShippingAddress;
import com.erp.mini.sales.repo.SalesOrderRepository;
import com.erp.mini.util.TestAuditorConfig;
import com.erp.mini.util.TestContainerManager;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseStatus;
import com.erp.mini.warehouse.repo.WarehouseRepository;

@SpringBootTest
@Import(TestAuditorConfig.class)
@ActiveProfiles("integration")
class InventoryTransactionServiceIntegrationTest {

    @Autowired
    private InventoryTransactionService inventoryTransactionService;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    @AfterEach
    void cleanup() {
        inventoryTransactionRepository.deleteAll();
        salesOrderRepository.deleteAll();
        itemRepository.deleteAll();
        warehouseRepository.deleteAll();
        partnerRepository.deleteAll();
    }

    private Item createItem(String name, String code) {
        Item i = Item.createItem(name, code, BigDecimal.valueOf(1000), ItemStatus.ACTIVE);
        return itemRepository.save(i);
    }

    private Warehouse createWarehouse(String name) {
        Warehouse w = Warehouse.createWarehouse(name, "loc", WarehouseStatus.ACTIVE);
        return warehouseRepository.save(w);
    }

    private Partner createCustomer(String code) {
        Partner p = Partner.createPartner(code, PartnerType.CUSTOMER, "010-1111-1111", code + "@co.kr");
        return partnerRepository.save(p);
    }

    private SalesOrder createSalesOrder(Partner customer) {
        SalesOrder so = SalesOrder.createSalesOrder(
                customer,
                new OrderCustomerInfo("고객", "010-2222-2222"),
                new ShippingAddress("00000", "서울", "강남"));

        return salesOrderRepository.save(so);
    }

    @Nested
    class getInventoryTransaction_test {
        @Test
        void list_all_and_filter() {
            Item item1 = createItem("A", "A001");
            Item item2 = createItem("B", "B001");
            Warehouse wh = createWarehouse("WH1");
            Partner cust = createCustomer("CUST1");
            SalesOrder so = createSalesOrder(cust);

            InventoryTransaction tx1 = InventoryTransaction.purchaseInbound(item1, wh, 10, 1L);
            InventoryTransaction tx2 = InventoryTransaction.salesOutbound(item2, wh, 5, so.getId());
            inventoryTransactionRepository.save(tx1);
            inventoryTransactionRepository.save(tx2);
            inventoryTransactionRepository.flush();

            PageResponse<ItxSearchDto> page = inventoryTransactionService.getInventoryTransaction(
                    new ItxSearchCondition(null, null, null, null, null), PageRequest.of(0, 10));
            assertThat(page.content()).hasSize(2);

            PageResponse<ItxSearchDto> itemFilter = inventoryTransactionService.getInventoryTransaction(
                    new ItxSearchCondition(item1.getId(), null, null, null, null), PageRequest.of(0, 10));
            assertThat(itemFilter.content()).hasSize(1);
            assertThat(itemFilter.content().get(0).itemCode()).isEqualTo("A001");
        }
    }

    @Nested
    class getInventoryTransactionDetail_test {
        @Test
        void detail_returns_correct_info() {
            Item item = createItem("A", "A001");
            Warehouse wh = createWarehouse("WH1");
            InventoryTransaction tx = InventoryTransaction.purchaseInbound(item, wh, 20, 2L);
            InventoryTransaction saved = inventoryTransactionRepository.save(tx);
            inventoryTransactionRepository.flush();

            ItxDetailResponse detail = inventoryTransactionService.getInventoryTransactionDetail(saved.getId());
            assertThat(detail).isNotNull();
            assertThat(detail.type()).isEqualTo(TransactionType.INBOUND);
            assertThat(detail.refType()).isEqualTo(RefType.PURCHASE_ORDER);
        }

        @Test
        void detail_null_when_not_found() {
            ItxDetailResponse detail = inventoryTransactionService.getInventoryTransactionDetail(999L);
            assertThat(detail).isNull();
        }
    }
}