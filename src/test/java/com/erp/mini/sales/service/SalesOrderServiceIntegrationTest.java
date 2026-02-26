package com.erp.mini.sales.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.inventory.domain.TransactionType;
import com.erp.mini.inventory.repo.InventoryTransactionRepository;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.item.repo.ItemRepository;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.partner.repo.PartnerRepository;
import com.erp.mini.sales.domain.OrderCustomerInfo;
import com.erp.mini.sales.domain.SalesOrder;
import com.erp.mini.sales.domain.SalesStatus;
import com.erp.mini.sales.domain.ShippingAddress;
import com.erp.mini.sales.dto.AddSalesOrderLineRequest;
import com.erp.mini.sales.dto.SalesOrderRequest;
import com.erp.mini.sales.repo.SalesOrderRepository;
import com.erp.mini.stock.domain.Stock;
import com.erp.mini.stock.repo.StockRepository;
import com.erp.mini.util.TestAuditorConfig;
import com.erp.mini.util.TestContainerManager;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseStatus;
import com.erp.mini.warehouse.repo.WarehouseRepository;

@SpringBootTest
@Import(TestAuditorConfig.class)
@ActiveProfiles("integration")
class SalesOrderServiceIntegrationTest {

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

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
        salesOrderRepository.deleteAll();
        itemRepository.deleteAll();
        warehouseRepository.deleteAll();
        partnerRepository.deleteAll();
    }

    private Partner createCustomer(String name, String code) {
        Partner p = Partner.createPartner(code, PartnerType.CUSTOMER, "010-1234-5678", name + "@co.kr");
        return partnerRepository.save(p);
    }

    private Partner createSupplier(String name, String code) {
        Partner p = Partner.createPartner(code, PartnerType.SUPPLIER, "010-1234-5678", name + "@co.kr");
        return partnerRepository.save(p);
    }

    private Item createItem(String name, String code) {
        Item it = Item.createItem(name, code, BigDecimal.valueOf(1000), ItemStatus.ACTIVE);
        return itemRepository.save(it);
    }

    private Item createInactiveItem(String name, String code) {
        Item it = Item.createItem(name, code, BigDecimal.valueOf(1000), ItemStatus.INACTIVE);
        return itemRepository.save(it);
    }

    private Warehouse createWarehouse(String name) {
        Warehouse w = Warehouse.createWarehouse(name, "loc", WarehouseStatus.ACTIVE);
        return warehouseRepository.save(w);
    }

    private Warehouse createInactiveWarehouse(String name) {
        Warehouse w = Warehouse.createWarehouse(name, "loc", WarehouseStatus.INACTIVE);
        return warehouseRepository.save(w);
    }

    private void prepareStock(Item item, Warehouse wh, long qty) {
        Stock stock = Stock.createStock(item, wh);
        stock.increase(qty);

        stockRepository.save(stock);
    }

    @Nested
    class createSale_test {

        @Test
        void createSale_success() {
            Partner customer = createCustomer("고객사", "CUST");
            Item item = createItem("상품A", "PROD-A");
            Warehouse wh = createWarehouse("창고1");

            SalesOrderRequest req = new SalesOrderRequest(
                    customer.getId(),
                    "김철수",
                    "010-9876-5432",
                    "12345",
                    "서울시",
                    "강남구",
                    Collections.singletonList(
                            new SalesOrderRequest.SaleLine(item.getId(), wh.getId(), BigDecimal.valueOf(1500), 5L)));

            salesOrderService.createSale(req);

            var sales = salesOrderRepository.findAll();
            assertThat(sales).hasSize(1);
            Long soId = sales.get(0).getId();
            var so = salesOrderRepository.findByIdWithLines(soId).orElseThrow();
            assertThat(so.getStatus()).isEqualTo(SalesStatus.CREATED);
            assertThat(so.getSalesOrderLines()).hasSize(1);
        }

        @Test
        void createSale_fail_partner_not_found() {
            Item item = createItem("상품B", "PROD-B");
            Warehouse wh = createWarehouse("창고2");

            SalesOrderRequest req = new SalesOrderRequest(
                    999L,
                    "김철수",
                    "010-9876-5432",
                    "12345",
                    "서울시",
                    "강남구",
                    Collections.singletonList(
                            new SalesOrderRequest.SaleLine(item.getId(), wh.getId(), BigDecimal.valueOf(1500), 5L)));

            assertThatThrownBy(() -> salesOrderService.createSale(req))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.NOT_FOUND);
        }

        @Test
        void createSale_fail_partner_is_supplier_not_customer() {
            Partner supplier = createSupplier("공급사", "SUPP");
            Item item = createItem("상품C", "PROD-C");
            Warehouse wh = createWarehouse("창고3");

            SalesOrderRequest req = new SalesOrderRequest(
                    supplier.getId(),
                    "김철수",
                    "010-9876-5432",
                    "12345",
                    "서울시",
                    "강남구",
                    Collections.singletonList(
                            new SalesOrderRequest.SaleLine(item.getId(), wh.getId(), BigDecimal.valueOf(1500), 5L)));

            assertThatThrownBy(() -> salesOrderService.createSale(req))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void createSale_fail_item_not_found() {
            Partner customer = createCustomer("고객사2", "CUST2");
            Warehouse wh = createWarehouse("창고4");

            SalesOrderRequest req = new SalesOrderRequest(
                    customer.getId(),
                    "김철수",
                    "010-9876-5432",
                    "12345",
                    "서울시",
                    "강남구",
                    Collections.singletonList(
                            new SalesOrderRequest.SaleLine(999L, wh.getId(), BigDecimal.valueOf(1500), 5L)));

            assertThatThrownBy(() -> salesOrderService.createSale(req))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.NOT_FOUND)
                    .hasMessageContaining("상품");
        }

        @Test
        void createSale_fail_warehouse_not_found() {
            Partner customer = createCustomer("고객사3", "CUST3");
            Item item = createItem("상품D", "PROD-D");

            SalesOrderRequest req = new SalesOrderRequest(
                    customer.getId(),
                    "김철수",
                    "010-9876-5432",
                    "12345",
                    "서울시",
                    "강남구",
                    Collections.singletonList(
                            new SalesOrderRequest.SaleLine(item.getId(), 999L, BigDecimal.valueOf(1500), 5L)));

            assertThatThrownBy(() -> salesOrderService.createSale(req))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.NOT_FOUND)
                    .hasMessageContaining("창고");
        }

        @Test
        void createSale_fail_item_inactive() {
            Partner customer = createCustomer("고객사4", "CUST4");
            Item inactiveItem = createInactiveItem("상품E", "PROD-E");
            Warehouse wh = createWarehouse("창고5");

            SalesOrderRequest req = new SalesOrderRequest(
                    customer.getId(),
                    "김철수",
                    "010-9876-5432",
                    "12345",
                    "서울시",
                    "강남구",
                    Collections.singletonList(new SalesOrderRequest.SaleLine(inactiveItem.getId(), wh.getId(),
                            BigDecimal.valueOf(1500), 5L)));

            assertThatThrownBy(() -> salesOrderService.createSale(req))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void createSale_fail_warehouse_inactive() {
            Partner customer = createCustomer("고객사5", "CUST5");
            Item item = createItem("상품F", "PROD-F");
            Warehouse inactiveWh = createInactiveWarehouse("창고6");

            SalesOrderRequest req = new SalesOrderRequest(
                    customer.getId(),
                    "김철수",
                    "010-9876-5432",
                    "12345",
                    "서울시",
                    "강남구",
                    Collections.singletonList(new SalesOrderRequest.SaleLine(item.getId(), inactiveWh.getId(),
                            BigDecimal.valueOf(1500), 5L)));

            assertThatThrownBy(() -> salesOrderService.createSale(req))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    class addOrderLine_test {

        @Test
        void addOrderLine_success() {
            Partner customer = createCustomer("고객사6", "CUST6");
            Item item1 = createItem("상품1", "PROD-1");
            Item item2 = createItem("상품2", "PROD-2");
            Warehouse wh = createWarehouse("창고7");

            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("김철수", "010-9876-5432"),
                    new ShippingAddress("12345", "서울", "강남"));
            so.addLine(item1, wh, 5L, BigDecimal.valueOf(1000));
            salesOrderRepository.save(so);

            AddSalesOrderLineRequest req = new AddSalesOrderLineRequest(item2.getId(), wh.getId(),
                    BigDecimal.valueOf(2000), 3L);
            salesOrderService.addOrderLine(so.getId(), req);

            var found = salesOrderRepository.findByIdWithLines(so.getId()).orElseThrow();
            assertThat(found.getSalesOrderLines()).hasSize(2);
        }

        @Test
        void addOrderLine_fail_sales_order_not_found() {
            Item item = createItem("상품3", "PROD-3");
            Warehouse wh = createWarehouse("창고8");

            AddSalesOrderLineRequest req = new AddSalesOrderLineRequest(item.getId(), wh.getId(),
                    BigDecimal.valueOf(2000), 3L);

            assertThatThrownBy(() -> salesOrderService.addOrderLine(999L, req))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.NOT_FOUND);
        }

        @Test
        void addOrderLine_fail_item_not_found() {
            Partner customer = createCustomer("고객사7", "CUST7");
            Item item = createItem("상품4", "PROD-4");
            Warehouse wh = createWarehouse("창고9");

            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("김철수", "010-9876-5432"),
                    new ShippingAddress("12345", "서울", "강남"));
            so.addLine(item, wh, 5L, BigDecimal.valueOf(1000));
            salesOrderRepository.save(so);

            AddSalesOrderLineRequest req = new AddSalesOrderLineRequest(999L, wh.getId(), BigDecimal.valueOf(2000), 3L);

            assertThatThrownBy(() -> salesOrderService.addOrderLine(so.getId(), req))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.NOT_FOUND);
        }
    }

    @Nested
    class removeSaleOrder_test {

        @Test
        void removeSaleOrder_success() {
            Partner customer = createCustomer("고객사8", "CUST8");
            Item item1 = createItem("상품5", "PROD-5");
            Item item2 = createItem("상품6", "PROD-6");
            Warehouse wh = createWarehouse("창고10");

            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("김철수", "010-9876-5432"),
                    new ShippingAddress("12345", "서울", "강남"));
            so.addLine(item1, wh, 5L, BigDecimal.valueOf(1000));
            so.addLine(item2, wh, 3L, BigDecimal.valueOf(2000));
            salesOrderRepository.save(so);

            Long lineIdToRemove = so.getSalesOrderLines().get(0).getId();
            salesOrderService.removeSaleOrder(so.getId(), lineIdToRemove);

            var found = salesOrderRepository.findByIdWithLines(so.getId()).orElseThrow();
            assertThat(found.getSalesOrderLines()).hasSize(1);
        }
    }

    @Nested
    class orderSales_test {

        @Test
        void orderSales_success_decreases_stock() {
            Partner customer = createCustomer("고객사9", "CUST9");
            Item item = createItem("상품7", "PROD-7");
            Warehouse wh = createWarehouse("창고11");

            prepareStock(item, wh, 100L);

            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("김철수", "010-9876-5432"),
                    new ShippingAddress("12345", "서울", "강남"));
            so.addLine(item, wh, 10L, BigDecimal.valueOf(1000));
            salesOrderRepository.save(so);

            salesOrderService.orderSales(so.getId());

            var stock = stockRepository.findAll().get(0);
            assertThat(stock.getQty()).isEqualTo(90L);

            var found = salesOrderRepository.findById(so.getId()).orElseThrow();
            assertThat(found.getStatus()).isEqualTo(SalesStatus.ORDERED);

            var txs = inventoryTransactionRepository.findAll();
            long outboundCount = txs.stream().filter(t -> t.getType() == TransactionType.OUTBOUND).count();
            assertThat(outboundCount).isEqualTo(1L);
        }

        @Test
        void orderSales_fail_stock_insufficient() {
            Partner customer = createCustomer("고객사10", "CUST10");
            Item item = createItem("상품8", "PROD-8");
            Warehouse wh = createWarehouse("창고12");

            prepareStock(item, wh, 5L);

            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("김철수", "010-9876-5432"),
                    new ShippingAddress("12345", "서울", "강남"));
            so.addLine(item, wh, 10L, BigDecimal.valueOf(1000));
            salesOrderRepository.save(so);

            assertThatThrownBy(() -> salesOrderService.orderSales(so.getId()))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.CONFLICT);
        }

        @Test
        void orderSales_fail_sales_order_not_found() {
            assertThatThrownBy(() -> salesOrderService.orderSales(999L))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.NOT_FOUND);
        }

        @Test
        void orderSales_fail_no_lines() {
            Partner customer = createCustomer("고객사11", "CUST11");

            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("김철수", "010-9876-5432"),
                    new ShippingAddress("12345", "서울", "강남"));
            salesOrderRepository.save(so);

            assertThatThrownBy(() -> salesOrderService.orderSales(so.getId()))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    class cancelSales_test {

        @Test
        void cancelSales_from_ordered_restores_stock() {
            Partner customer = createCustomer("고객사12", "CUST12");
            Item item = createItem("상품9", "PROD-9");
            Warehouse wh = createWarehouse("창고13");

            prepareStock(item, wh, 100L);

            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("김철수", "010-9876-5432"),
                    new ShippingAddress("12345", "서울", "강남"));
            so.addLine(item, wh, 10L, BigDecimal.valueOf(1000));
            salesOrderRepository.save(so);

            salesOrderService.orderSales(so.getId());
            assertThat(stockRepository.findAll().get(0).getQty()).isEqualTo(90L);

            salesOrderService.cancelSales(so.getId());

            var stock = stockRepository.findAll().get(0);
            assertThat(stock.getQty()).isEqualTo(100L);

            var found = salesOrderRepository.findById(so.getId()).orElseThrow();
            assertThat(found.getStatus()).isEqualTo(SalesStatus.CANCELLED);

            var txs = inventoryTransactionRepository.findAll();
            long inboundCount = txs.stream().filter(t -> t.getType() == TransactionType.INBOUND).count();
            assertThat(inboundCount).isEqualTo(1L); // 취소복구
        }

        @Test
        void cancelSales_from_created_does_not_restore() {
            Partner customer = createCustomer("고객사13", "CUST13");
            Item item = createItem("상품10", "PROD-10");
            Warehouse wh = createWarehouse("창고14");

            prepareStock(item, wh, 100L);

            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("김철수", "010-9876-5432"),
                    new ShippingAddress("12345", "서울", "강남"));
            so.addLine(item, wh, 10L, BigDecimal.valueOf(1000));
            salesOrderRepository.save(so);

            salesOrderService.cancelSales(so.getId());

            var stock = stockRepository.findAll().get(0);
            assertThat(stock.getQty()).isEqualTo(100L);

            var txs = inventoryTransactionRepository.findAll();
            long outboundCount = txs.stream().filter(t -> t.getType() == TransactionType.OUTBOUND).count();
            assertThat(outboundCount).isEqualTo(0L);
        }

        @Test
        void cancelSales_fail_sales_order_not_found() {
            assertThatThrownBy(() -> salesOrderService.cancelSales(999L))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.NOT_FOUND);
        }
    }

    @Nested
    class shipped_test {

        @Test
        void shipped_success() {
            Partner customer = createCustomer("고객사14", "CUST14");
            Item item = createItem("상품11", "PROD-11");
            Warehouse wh = createWarehouse("창고15");

            prepareStock(item, wh, 100L);

            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("김철수", "010-9876-5432"),
                    new ShippingAddress("12345", "서울", "강남"));
            so.addLine(item, wh, 5L, BigDecimal.valueOf(1000));
            salesOrderRepository.save(so);

            salesOrderService.orderSales(so.getId());

            salesOrderService.shipped(so.getId());

            var found = salesOrderRepository.findById(so.getId()).orElseThrow();
            assertThat(found.getStatus()).isEqualTo(SalesStatus.SHIPPED);
        }

        @Test
        void shipped_fail_not_ordered() {
            Partner customer = createCustomer("고객사15", "CUST15");
            Item item = createItem("상품12", "PROD-12");
            Warehouse wh = createWarehouse("창고16");

            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("김철수", "010-9876-5432"),
                    new ShippingAddress("12345", "서울", "강남"));
            so.addLine(item, wh, 5L, BigDecimal.valueOf(1000));
            salesOrderRepository.save(so);

            assertThatThrownBy(() -> salesOrderService.shipped(so.getId()))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void shipped_fail_sales_order_not_found() {
            assertThatThrownBy(() -> salesOrderService.shipped(999L))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.NOT_FOUND);
        }
    }
}
