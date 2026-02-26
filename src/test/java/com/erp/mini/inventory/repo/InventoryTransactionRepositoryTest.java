package com.erp.mini.inventory.repo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.erp.mini.common.config.JpaConfig;
import com.erp.mini.inventory.domain.InventoryTransaction;
import com.erp.mini.inventory.domain.RefType;
import com.erp.mini.inventory.domain.TransactionType;
import com.erp.mini.inventory.dto.ItxDetailResponse;
import com.erp.mini.inventory.dto.ItxSearchCondition;
import com.erp.mini.inventory.dto.ItxSearchDto;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.purchase.domain.PurchaseOrder;
import com.erp.mini.sales.domain.OrderCustomerInfo;
import com.erp.mini.sales.domain.SalesOrder;
import com.erp.mini.sales.domain.ShippingAddress;
import com.erp.mini.util.TestAuditorConfig;
import com.erp.mini.util.TestContainerManager;
import com.erp.mini.util.TestQuerydslConfig;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ InventoryTransactionRepositoryImpl.class, JpaConfig.class, TestAuditorConfig.class, TestQuerydslConfig.class })
@ActiveProfiles("integration")
public class InventoryTransactionRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    private Item saveItem(String name, String code) {
        Item item = Item.createItem(name, code, BigDecimal.valueOf(10000), ItemStatus.ACTIVE);
        em.persist(item);
        return item;
    }

    private Warehouse saveWarehouse(String name, String location) {
        Warehouse warehouse = Warehouse.createWarehouse(name, location, WarehouseStatus.ACTIVE);
        em.persist(warehouse);
        return warehouse;
    }

    private Partner savePartner(String code, PartnerType type, String phone, String contactEmail) {
        Partner partner = Partner.createPartner(code, type, phone, contactEmail);
        em.persist(partner);
        return partner;
    }

    private PurchaseOrder savePurchaseOrder(Partner supplier) {
        PurchaseOrder po = PurchaseOrder.createPurchaseOrder(supplier);
        em.persist(po);
        return po;
    }

    private SalesOrder saveSalesOrder(Partner customer) {
        SalesOrder so = SalesOrder.createSalesOrder(
                customer,
                new OrderCustomerInfo("고객명", "010-1234-5678"),
                new ShippingAddress("12345", "서울시 강남구", "테헤란로 123")
        );
        em.persist(so);
        return so;
    }

    @Nested
    class findInventoryTransaction_test {
        @Test
        void findInventoryTransaction_success_no_filter() {
            // given
            Item item = saveItem("설탕", "SUGAR001");
            Warehouse warehouse = saveWarehouse("서울창고", "서울시");
            Partner supplier = savePartner("SUP001", PartnerType.SUPPLIER, "010-1111-1111", "supplier@co.kr");
            Partner customer = savePartner("CUST001", PartnerType.CUSTOMER, "010-2222-2222", "customer@co.kr");
            PurchaseOrder po = savePurchaseOrder(supplier);
            SalesOrder so = saveSalesOrder(customer);

            InventoryTransaction tx1 = InventoryTransaction.purchaseInbound(item, warehouse, 100, po.getId());
            InventoryTransaction tx2 = InventoryTransaction.salesOutbound(item, warehouse, 50, so.getId());

            em.persist(tx1);
            em.persist(tx2);
            em.flush();
            em.clear();

            // when
            Page<ItxSearchDto> result = inventoryTransactionRepository.findInventoryTransaction(
                    new ItxSearchCondition(null, null, null, null, null),
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        void findInventoryTransaction_success_filter_by_item() {
            // given
            Item item1 = saveItem("설탕", "SUGAR001");
            Item item2 = saveItem("소금", "SALT001");
            Warehouse warehouse = saveWarehouse("서울창고", "서울시");
            Partner supplier = savePartner("SUP001", PartnerType.SUPPLIER, "010-1111-1111", "supplier@co.kr");
            PurchaseOrder po = savePurchaseOrder(supplier);

            InventoryTransaction tx1 = InventoryTransaction.purchaseInbound(item1, warehouse, 100, po.getId());
            InventoryTransaction tx2 = InventoryTransaction.purchaseInbound(item2, warehouse, 50, po.getId());

            em.persist(tx1);
            em.persist(tx2);
            em.flush();
            em.clear();

            // when
            Page<ItxSearchDto> result = inventoryTransactionRepository.findInventoryTransaction(
                    new ItxSearchCondition(item1.getId(), null, null, null, null),
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).itemCode()).isEqualTo("SUGAR001");
        }

        @Test
        void findInventoryTransaction_success_filter_by_warehouse() {
            // given
            Item item = saveItem("설탕", "SUGAR001");
            Warehouse warehouse1 = saveWarehouse("서울창고", "서울시");
            Warehouse warehouse2 = saveWarehouse("부산창고", "부산시");
            Partner supplier = savePartner("SUP001", PartnerType.SUPPLIER, "010-1111-1111", "supplier@co.kr");
            PurchaseOrder po = savePurchaseOrder(supplier);

            InventoryTransaction tx1 = InventoryTransaction.purchaseInbound(item, warehouse1, 100, po.getId());
            InventoryTransaction tx2 = InventoryTransaction.purchaseInbound(item, warehouse2, 50, po.getId());

            em.persist(tx1);
            em.persist(tx2);
            em.flush();
            em.clear();

            // when
            Page<ItxSearchDto> result = inventoryTransactionRepository.findInventoryTransaction(
                    new ItxSearchCondition(null, warehouse1.getId(), null, null, null),
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).warehouseCode()).isEqualTo(warehouse1.getCode());
        }

        @Test
        void findInventoryTransaction_success_filter_by_type() {
            // given
            Item item = saveItem("설탕", "SUGAR001");
            Warehouse warehouse = saveWarehouse("서울창고", "서울시");
            Partner supplier = savePartner("SUP001", PartnerType.SUPPLIER, "010-1111-1111", "supplier@co.kr");
            Partner customer = savePartner("CUST001", PartnerType.CUSTOMER, "010-2222-2222", "customer@co.kr");
            PurchaseOrder po = savePurchaseOrder(supplier);
            SalesOrder so = saveSalesOrder(customer);

            InventoryTransaction tx1 = InventoryTransaction.purchaseInbound(item, warehouse, 100, po.getId());
            InventoryTransaction tx2 = InventoryTransaction.salesOutbound(item, warehouse, 50, so.getId());

            em.persist(tx1);
            em.persist(tx2);
            em.flush();
            em.clear();

            // when
            Page<ItxSearchDto> result = inventoryTransactionRepository.findInventoryTransaction(
                    new ItxSearchCondition(null, null, null, null, TransactionType.INBOUND),
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).type()).isEqualTo(TransactionType.INBOUND);
        }

        @Test
        void findInventoryTransaction_success_by_date_range() {
            // given
            Item item = saveItem("설탕", "SUGAR001");
            Warehouse warehouse = saveWarehouse("서울창고", "서울시");
            Partner supplier = savePartner("SUP001", PartnerType.SUPPLIER, "010-1111-1111", "supplier@co.kr");
            PurchaseOrder po = savePurchaseOrder(supplier);

            InventoryTransaction tx = InventoryTransaction.purchaseInbound(item, warehouse, 100, po.getId());
            em.persist(tx);
            em.flush();
            em.clear();

            LocalDate today = LocalDate.now();

            // when
            Page<ItxSearchDto> result = inventoryTransactionRepository.findInventoryTransaction(
                    new ItxSearchCondition(null, null, today, today, null),
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        void findInventoryTransaction_success_pagination() {
            // given
            Item item = saveItem("설탕", "SUGAR001");
            Warehouse warehouse = saveWarehouse("서울창고", "서울시");
            Partner supplier = savePartner("SUP001", PartnerType.SUPPLIER, "010-1111-1111", "supplier@co.kr");
            PurchaseOrder po = savePurchaseOrder(supplier);

            for (int i = 0; i < 15; i++) {
                InventoryTransaction tx = InventoryTransaction.purchaseInbound(item, warehouse, 100, po.getId());
                em.persist(tx);
            }
            em.flush();
            em.clear();

            // when
            Page<ItxSearchDto> page1 = inventoryTransactionRepository.findInventoryTransaction(
                    new ItxSearchCondition(null, null, null, null, null),
                    PageRequest.of(0, 10)
            );
            Page<ItxSearchDto> page2 = inventoryTransactionRepository.findInventoryTransaction(
                    new ItxSearchCondition(null, null, null, null, null),
                    PageRequest.of(1, 10)
            );

            // then
            assertThat(page1.getContent()).hasSize(10);
            assertThat(page2.getContent()).hasSize(5);
            assertThat(page1.getTotalElements()).isEqualTo(15);
        }
    }

    @Nested
    class findInventoryTransactionDetail_test {
        @Test
        void findInventoryTransactionDetail_success_with_purchase_inbound() {
            // given
            Item item = saveItem("설탕", "SUGAR001");
            Warehouse warehouse = saveWarehouse("서울창고", "서울시");
            Partner supplier = savePartner("SUP001", PartnerType.SUPPLIER, "010-1111-1111", "supplier@co.kr");
            PurchaseOrder po = savePurchaseOrder(supplier);

            InventoryTransaction tx = InventoryTransaction.purchaseInbound(item, warehouse, 100, po.getId());
            em.persist(tx);
            em.flush();
            em.clear();

            // when
            ItxDetailResponse detail = inventoryTransactionRepository.findInventoryTransactionDetail(tx.getId());

            // then
            assertThat(detail).isNotNull();
            assertThat(detail.type()).isEqualTo(TransactionType.INBOUND);
            assertThat(detail.refType()).isEqualTo(RefType.PURCHASE_ORDER);
            assertThat(detail.refId()).isEqualTo(po.getId());
            assertThat(detail.partnerCode()).isEqualTo(supplier.getCode());
            assertThat(detail.partnerName()).isEqualTo(supplier.getName());
        }

        @Test
        void findInventoryTransactionDetail_success_with_sales_outbound() {
            // given
            Item item = saveItem("설탕", "SUGAR001");
            Warehouse warehouse = saveWarehouse("서울창고", "서울시");
            Partner customer = savePartner("CUST001", PartnerType.CUSTOMER, "010-2222-2222", "customer@co.kr");
            SalesOrder so = saveSalesOrder(customer);

            InventoryTransaction tx = InventoryTransaction.salesOutbound(item, warehouse, 50, so.getId());
            em.persist(tx);
            em.flush();
            em.clear();

            // when
            ItxDetailResponse detail = inventoryTransactionRepository.findInventoryTransactionDetail(tx.getId());

            // then
            assertThat(detail).isNotNull();
            assertThat(detail.type()).isEqualTo(TransactionType.OUTBOUND);
            assertThat(detail.refType()).isEqualTo(RefType.SALES_ORDER);
            assertThat(detail.refId()).isEqualTo(so.getId());
            assertThat(detail.partnerCode()).isEqualTo(customer.getCode());
            assertThat(detail.partnerName()).isEqualTo(customer.getName());
        }

        @Test
        void findInventoryTransactionDetail_success_with_cancel_sales_inbound() {
            // given
            Item item = saveItem("설탕", "SUGAR001");
            Warehouse warehouse = saveWarehouse("서울창고", "서울시");
            Partner customer = savePartner("CUST001", PartnerType.CUSTOMER, "010-2222-2222", "customer@co.kr");
            SalesOrder so = saveSalesOrder(customer);

            InventoryTransaction tx = InventoryTransaction.cancelSalesInbound(item, warehouse, 30, so.getId());
            em.persist(tx);
            em.flush();
            em.clear();

            // when
            ItxDetailResponse detail = inventoryTransactionRepository.findInventoryTransactionDetail(tx.getId());

            // then
            assertThat(detail).isNotNull();
            assertThat(detail.type()).isEqualTo(TransactionType.INBOUND);
            assertThat(detail.refType()).isEqualTo(RefType.SALES_ORDER);
            assertThat(detail.refId()).isEqualTo(so.getId());
            assertThat(detail.partnerCode()).isEqualTo(customer.getCode());
            assertThat(detail.partnerName()).isEqualTo(customer.getName());
        }

        @Test
        void findInventoryTransactionDetail_success_with_adjust() {
            // given
            Item item = saveItem("설탕", "SUGAR001");
            Warehouse warehouse = saveWarehouse("서울창고", "서울시");

            InventoryTransaction tx = InventoryTransaction.adjust(item, warehouse, -5, "손상");
            em.persist(tx);
            em.flush();
            em.clear();

            // when
            ItxDetailResponse detail = inventoryTransactionRepository.findInventoryTransactionDetail(tx.getId());

            // then
            assertThat(detail).isNotNull();
            assertThat(detail.type()).isEqualTo(TransactionType.ADJUST);
            assertThat(detail.refType()).isNull();
            assertThat(detail.refId()).isNull();
            assertThat(detail.reason()).isEqualTo("손상");
            assertThat(detail.partnerCode()).isNull();
        }

        @Test
        void findInventoryTransactionDetail_return_null_when_not_found() {
            // when
            ItxDetailResponse detail = inventoryTransactionRepository.findInventoryTransactionDetail(99999L);

            // then
            assertThat(detail).isNull();
        }
    }
}

