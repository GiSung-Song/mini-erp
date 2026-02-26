package com.erp.mini.sales.repo;

import com.erp.mini.common.config.JpaConfig;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.sales.domain.OrderCustomerInfo;
import com.erp.mini.sales.domain.SalesOrder;
import com.erp.mini.sales.domain.ShippingAddress;
import com.erp.mini.sales.dto.SalesHeaderDto;
import com.erp.mini.sales.dto.SalesLineDto;
import com.erp.mini.util.TestAuditorConfig;
import com.erp.mini.util.TestContainerManager;
import com.erp.mini.util.TestQuerydslConfig;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SalesOrderRepositoryImpl.class, JpaConfig.class, TestAuditorConfig.class, TestQuerydslConfig.class})
@ActiveProfiles("integration")
class SalesOrderRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    private Partner saveCustomer(String name, String code, String phone) {
        Partner customer = Partner.createPartner(code, PartnerType.CUSTOMER, phone, name + "@co.kr");
        em.persist(customer);
        return customer;
    }

    private Item saveItem(String name, String code, BigDecimal price) {
        Item item = Item.createItem(name, code, price, ItemStatus.ACTIVE);
        em.persist(item);
        return item;
    }

    private Warehouse saveWarehouse(String name, String code, String location) {
        Warehouse warehouse = Warehouse.createWarehouse(name, location, WarehouseStatus.ACTIVE);
        em.persist(warehouse);
        return warehouse;
    }

    private SalesOrder saveSalesOrder(Partner customer, String customerName, String customerPhone,
                                      String zipCode, String address1, String address2) {
        SalesOrder so = SalesOrder.createSalesOrder(
                customer,
                new OrderCustomerInfo(customerName, customerPhone),
                new ShippingAddress(zipCode, address1, address2)
        );
        em.persist(so);
        return so;
    }

    @Test
    void findByIdWithLines_test() {
        Partner customer = saveCustomer("고객사", "CUST001", "010-1234-5678");
        Item item = saveItem("상품A", "PROD-A", BigDecimal.valueOf(10000));
        Warehouse warehouse = saveWarehouse("창고A", "WH-A", "서울시");

        SalesOrder so = saveSalesOrder(customer, "김철수", "010-9876-5432", "12345", "서울시", "강남구");
        so.addLine(item, warehouse, 5L, BigDecimal.valueOf(10000));

        em.persist(so);
        em.flush();
        em.clear();

        Optional<SalesOrder> result = salesOrderRepository.findByIdWithLines(so.getId());

        assertThat(result).isNotEmpty();
        assertThat(result.get().getSalesOrderLines()).hasSize(1);
        assertThat(result.get().getSalesOrderLines().get(0).getItem().getCode()).isEqualTo(item.getCode());
    }

    @Test
    void getSalesDetailHeader_test() {
        Partner customer = saveCustomer("고객사", "CUST002", "010-2222-2222");
        SalesOrder so = saveSalesOrder(customer, "이순신", "010-1111-1111", "54321", "부산시", "중구");

        em.flush();
        em.clear();

        SalesHeaderDto result = salesOrderRepository.getSalesDetailHeader(so.getId());

        assertThat(result).isNotNull();
        assertThat(result.salesOrderId()).isEqualTo(so.getId());
        assertThat(result.partnerCode()).isEqualTo(customer.getCode());
        assertThat(result.partnerName()).isEqualTo(customer.getName());
        assertThat(result.customerName()).isEqualTo("이순신");
        assertThat(result.customerPhone()).isEqualTo("010-1111-1111");
    }

    @Test
    void getSalesDetailLines_test() {
        Partner customer = saveCustomer("고객사", "CUST003", "010-3333-3333");
        Item item1 = saveItem("상품A", "PROD-A", BigDecimal.valueOf(5000));
        Item item2 = saveItem("상품B", "PROD-B", BigDecimal.valueOf(8000));
        Warehouse warehouse = saveWarehouse("창고B", "WH-B", "인천시");

        SalesOrder so = saveSalesOrder(customer, "강감찬", "010-4444-4444", "22222", "인천시", "연수구");
        so.addLine(item1, warehouse, 10L, BigDecimal.valueOf(5000));
        so.addLine(item2, warehouse, 5L, BigDecimal.valueOf(8000));

        em.persist(so);
        em.flush();
        em.clear();

        List<SalesLineDto> result = salesOrderRepository.getSalesDetailLines(so.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).itemCode()).isEqualTo("PROD-A");
        assertThat(result.get(0).qty()).isEqualTo(10L);
        assertThat(result.get(1).itemCode()).isEqualTo("PROD-B");
        assertThat(result.get(1).qty()).isEqualTo(5L);
    }

    @Test
    void saveAndFindById_test() {
        Partner customer = saveCustomer("고객사", "CUST004", "010-5555-5555");
        Item item = saveItem("상품C", "PROD-C", BigDecimal.valueOf(15000));
        Warehouse warehouse = saveWarehouse("창고C", "WH-C", "대구시");

        SalesOrder so = saveSalesOrder(customer, "을지문덕", "010-6666-6666", "33333", "대구시", "중구");
        so.addLine(item, warehouse, 3L, BigDecimal.valueOf(15000));

        em.persist(so);
        em.flush();
        em.clear();

        Optional<SalesOrder> result = salesOrderRepository.findById(so.getId());

        assertThat(result).isNotEmpty();
        assertThat(result.get().getPartner().getCode()).isEqualTo(customer.getCode());
        assertThat(result.get().getOrderCustomerInfo().getCustomerName()).isEqualTo("을지문덕");
    }
}
