package com.erp.mini.purchase.service;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.inventory.repo.InventoryTransactionRepository;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.item.repo.ItemRepository;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.partner.repo.PartnerRepository;
import com.erp.mini.purchase.domain.PurchaseOrder;
import com.erp.mini.purchase.domain.PurchaseStatus;
import com.erp.mini.purchase.repo.PurchaseOrderRepository;
import com.erp.mini.stock.repo.StockRepository;
import com.erp.mini.util.TestAuditorConfig;
import com.erp.mini.util.TestContainerManager;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseStatus;
import com.erp.mini.warehouse.repo.WarehouseRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestAuditorConfig.class)
@ActiveProfiles("integration")
class PurchaseOrderServiceIntegrationTest {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    @AfterEach
    void cleanup() {
        inventoryTransactionRepository.deleteAll();
        stockRepository.deleteAll();
        purchaseOrderRepository.deleteAll();
        itemRepository.deleteAll();
        warehouseRepository.deleteAll();
        partnerRepository.deleteAll();
    }

    private Partner createPartner() {
        Partner p = Partner.createPartner("SUP", PartnerType.SUPPLIER, "010-1234-5678", "sup@company.com");
        return partnerRepository.save(p);
    }

    private Item createItem(String name, String code) {
        Item it = Item.createItem(name, code, BigDecimal.valueOf(1000), ItemStatus.ACTIVE);
        return itemRepository.save(it);
    }

    private Warehouse createWarehouse(String name) {
        Warehouse w = Warehouse.createWarehouse(name, "loc", WarehouseStatus.ACTIVE);
        return warehouseRepository.save(w);
    }

    @Nested
    class receive_test {

        @Test
        void receive_success_creates_stock_and_transaction() {
            Partner supplier = createPartner();
            Item item = createItem("A", "ITM-A");
            Warehouse wh = createWarehouse("WH-A");

            PurchaseOrder po = PurchaseOrder.createPurchaseOrder(supplier);
            po.addLine(item, wh, 7L, BigDecimal.valueOf(1000));
            po.markAsOrdered();
            purchaseOrderRepository.save(po);

            purchaseOrderService.receive(po.getId());

            PurchaseOrder found = purchaseOrderRepository.findById(po.getId()).orElseThrow();
            assertThat(found.getStatus()).isEqualTo(PurchaseStatus.RECEIVED);

            var stocks = stockRepository.findAll();
            assertThat(stocks).hasSize(1);
            assertThat(stocks.get(0).getQty()).isEqualTo(7L);

            var txs = inventoryTransactionRepository.findAll();
            assertThat(txs).hasSize(1);
            assertThat(txs.get(0).getQtyDelta()).isEqualTo(7L);
            assertThat(txs.get(0).getRefId()).isEqualTo(po.getId());
        }

        @Test
        void receive_fail_when_not_ordered() {
            Partner supplier = createPartner();
            Item item = createItem("B", "ITM-B");
            Warehouse wh = createWarehouse("WH-B");

            PurchaseOrder po = PurchaseOrder.createPurchaseOrder(supplier);
            po.addLine(item, wh, 3L, BigDecimal.valueOf(1000));
            purchaseOrderRepository.save(po);

            assertThatThrownBy(() -> purchaseOrderService.receive(po.getId()))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.BAD_REQUEST);
        }
    }
}