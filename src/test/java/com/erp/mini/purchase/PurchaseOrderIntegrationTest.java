package com.erp.mini.purchase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.item.repo.ItemRepository;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.partner.repo.PartnerRepository;
import com.erp.mini.purchase.domain.PurchaseOrder;
import com.erp.mini.purchase.domain.PurchaseOrderTestDataFactory;
import com.erp.mini.purchase.domain.PurchaseStatus;
import com.erp.mini.purchase.dto.AddPurchaseOrderLineRequest;
import com.erp.mini.purchase.dto.PurchaseOrderRequest;
import com.erp.mini.purchase.repo.PurchaseOrderRepository;
import com.erp.mini.user.domain.User;
import com.erp.mini.user.domain.UserTestDataFactory;
import com.erp.mini.util.IntegrationTest;
import com.erp.mini.util.TestContainerManager;
import com.erp.mini.util.TestLoginUser;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseStatus;
import com.erp.mini.warehouse.repo.WarehouseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@IntegrationTest
public class PurchaseOrderIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserTestDataFactory userTestDataFactory;

        @Autowired
        private PurchaseOrderTestDataFactory purchaseOrderTestDataFactory;

        @Autowired
        private PurchaseOrderRepository repository;

        @Autowired
        private PartnerRepository partnerRepository;

        @Autowired
        private ItemRepository itemRepository;

        @Autowired
        private WarehouseRepository warehouseRepository;

        private Authentication auth;

        private Partner createPartner() {
                Partner p = Partner.createPartner("SUP", PartnerType.SUPPLIER, "010-0000-0000", "sup@company.com");
                return partnerRepository.save(p);
        }

        private Item createItem(String name, String code) {
                Item it = Item.createItem(name, code, BigDecimal.valueOf(1000), ItemStatus.ACTIVE);
                return itemRepository.save(it);
        }

        private Warehouse createWarehouse(String name, String location, WarehouseStatus status) {
                Warehouse w = Warehouse.createWarehouse(name, location, status);
                return warehouseRepository.save(w);
        }

        @DynamicPropertySource
        static void properties(DynamicPropertyRegistry registry) {
                TestContainerManager.registerMySQL(registry);
        }

        @BeforeEach
        void setUp() {
                User user = userTestDataFactory.createUser("tester", "EMP-0001");

                auth = TestLoginUser.setAuthLogin(user);
        }

        @Nested
        class create_purchase_order_test {
                @Test
                void create_purchase_order_success() throws Exception {
                        Partner supplier = createPartner();
                        Item item1 = createItem("ITEM1", "CODE1");
                        Item item2 = createItem("ITEM2", "CODE2");
                        Warehouse wh = createWarehouse("WH1", "loc", WarehouseStatus.ACTIVE);

                        PurchaseOrderRequest request = new PurchaseOrderRequest(
                                        supplier.getId(),
                                        List.of(
                                                        new PurchaseOrderRequest.PurchaseLine(
                                                                        item1.getId(), wh.getId(),
                                                                        BigDecimal.valueOf(1500), 10L),
                                                        new PurchaseOrderRequest.PurchaseLine(
                                                                        item2.getId(), wh.getId(),
                                                                        BigDecimal.valueOf(2500), 10L)));

                        mockMvc.perform(post("/api/purchase-order")
                                        .with(authentication(auth))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andDo(print());

                        PurchaseOrder purchaseOrder = repository.findAll().get(0);

                        assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseStatus.CREATED);
                        assertThat(purchaseOrder.getPurchaseOrderLines()).hasSize(2);
                }

                @Test
                void create_purchase_order_fail_with_missing_fields() throws Exception {
                        Partner supplier = createPartner();
                        Item item = createItem("ITEM1", "CODE1");
                        Warehouse wh = createWarehouse("WH1", "loc", WarehouseStatus.ACTIVE);

                        PurchaseOrderRequest request = new PurchaseOrderRequest(
                                        supplier.getId(),
                                        List.of(
                                                        new PurchaseOrderRequest.PurchaseLine(
                                                                        item.getId(), wh.getId(),
                                                                        BigDecimal.valueOf(1500), 10L),
                                                        new PurchaseOrderRequest.PurchaseLine(
                                                                        null, wh.getId(), BigDecimal.valueOf(2500),
                                                                        10L)));

                        mockMvc.perform(post("/api/purchase-order")
                                        .with(authentication(auth))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andDo(print());
                }

                @Test
                void create_purchase_order_fail_with_warehouse_is_inactive() throws Exception {
                        Partner supplier = createPartner();
                        Item item1 = createItem("ITEM1", "CODE1");
                        Item item2 = createItem("ITEM2", "CODE2");
                        Warehouse activeWh = createWarehouse("WH1", "loc", WarehouseStatus.ACTIVE);
                        Warehouse inactiveWh = createWarehouse("WH2", "loc2", WarehouseStatus.INACTIVE);

                        PurchaseOrderRequest request = new PurchaseOrderRequest(
                                        supplier.getId(),
                                        List.of(
                                                        new PurchaseOrderRequest.PurchaseLine(
                                                                        item1.getId(), activeWh.getId(),
                                                                        BigDecimal.valueOf(1500), 10L),
                                                        new PurchaseOrderRequest.PurchaseLine(
                                                                        item2.getId(), inactiveWh.getId(),
                                                                        BigDecimal.valueOf(2500), 10L)));

                        mockMvc.perform(post("/api/purchase-order")
                                        .with(authentication(auth))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andDo(print());
                }
        }

        @Nested
        class add_purchase_order {
                @Test
                void add_purchase_order_success() throws Exception {
                        PurchaseOrder purchaseOrder = purchaseOrderTestDataFactory.createPurchaseOrder();
                        // create a new item and use same warehouse as existing line
                        Item newItem = createItem("ITEM2", "CODE2");
                        Long warehouseId = purchaseOrder.getPurchaseOrderLines().get(0).getWarehouse().getId();

                        AddPurchaseOrderLineRequest request = new AddPurchaseOrderLineRequest(
                                        newItem.getId(), warehouseId, BigDecimal.valueOf(2500), 10L);

                        mockMvc.perform(post("/api/purchase-order/{purchaseOrderId}", purchaseOrder.getId())
                                        .with(authentication(auth))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andDo(print());

                        PurchaseOrder editedOrder = repository.findById(purchaseOrder.getId())
                                        .orElseThrow();

                        assertThat(editedOrder.getPurchaseOrderLines()).hasSize(2);
                }

                @Test
                void add_purchase_order_fail_with_missing_fields() throws Exception {
                        PurchaseOrder purchaseOrder = purchaseOrderTestDataFactory.createPurchaseOrder();
                        Item newItem = createItem("ITEM2", "CODE2");

                        AddPurchaseOrderLineRequest request = new AddPurchaseOrderLineRequest(
                                        newItem.getId(), null, BigDecimal.valueOf(2500), 10L);

                        mockMvc.perform(post("/api/purchase-order/{purchaseOrderId}", purchaseOrder.getId())
                                        .with(authentication(auth))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andDo(print());
                }

                @Test
                void add_purchase_order_fail_with_warehouse_is_inactive() throws Exception {
                        PurchaseOrder purchaseOrder = purchaseOrderTestDataFactory.createPurchaseOrder();
                        Item newItem = createItem("ITEM2", "CODE2");
                        // create inactive warehouse
                        Warehouse inactiveWh = createWarehouse("WH_IN", "loc", WarehouseStatus.INACTIVE);

                        AddPurchaseOrderLineRequest request = new AddPurchaseOrderLineRequest(
                                        newItem.getId(), inactiveWh.getId(), BigDecimal.valueOf(2500), 10L);

                        mockMvc.perform(post("/api/purchase-order/{purchaseOrderId}", purchaseOrder.getId())
                                        .with(authentication(auth))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andDo(print());
                }
        }

        @Nested
        class remove_purchase_order_test {
                @Test
                void remove_purchase_order_success() throws Exception {
                        PurchaseOrder purchaseOrder = purchaseOrderTestDataFactory.createPurchaseOrder();

                        Long purchaseOrderLineId = purchaseOrder.getPurchaseOrderLines().get(0).getId();

                        mockMvc.perform(delete("/api/purchase-order/{purchaseOrderId}/line/{purchaseOrderLineId}",
                                        purchaseOrder.getId(), purchaseOrderLineId)
                                        .with(authentication(auth)))
                                        .andExpect(status().isOk())
                                        .andDo(print());

                        PurchaseOrder editedOrder = repository.findById(purchaseOrder.getId()).orElseThrow();

                        assertThat(editedOrder.getPurchaseOrderLines()).isEmpty();
                }

                @Test
                void remove_purchase_order_fail_with_not_found_line() throws Exception {
                        PurchaseOrder purchaseOrder = purchaseOrderTestDataFactory.createPurchaseOrder();

                        Long anotherId = purchaseOrder.getPurchaseOrderLines().get(0).getId() + 10L;

                        mockMvc.perform(delete("/api/purchase-order/{purchaseOrderId}/line/{purchaseOrderLineId}",
                                        purchaseOrder.getId(), anotherId)
                                        .with(authentication(auth)))
                                        .andExpect(status().isNotFound())
                                        .andDo(print());
                }
        }

        @Nested
        class order_purchase_test {
                @Test
                void order_purchase_success() throws Exception {
                        PurchaseOrder purchaseOrder = purchaseOrderTestDataFactory.createPurchaseOrder();

                        mockMvc.perform(patch("/api/purchase-order/{purchaseOrderId}/order", purchaseOrder.getId())
                                        .with(authentication(auth)))
                                        .andExpect(status().isOk())
                                        .andDo(print());

                        PurchaseOrder ordered = repository.findById(purchaseOrder.getId()).orElseThrow();

                        assertThat(ordered.getStatus()).isEqualTo(PurchaseStatus.ORDERED);
                }
        }

        @Nested
        class cancel_purchase_test {
                @Test
                void cancel_purchase_order_success() throws Exception {
                        PurchaseOrder purchaseOrder = purchaseOrderTestDataFactory.createPurchaseOrder();

                        mockMvc.perform(patch("/api/purchase-order/{purchaseOrderId}/cancel", purchaseOrder.getId())
                                        .with(authentication(auth)))
                                        .andExpect(status().isOk())
                                        .andDo(print());

                        PurchaseOrder ordered = repository.findById(purchaseOrder.getId()).orElseThrow();

                        assertThat(ordered.getStatus()).isEqualTo(PurchaseStatus.CANCELLED);
                }
        }

        @Nested
        class get_purchase_detail_test {
                @Test
                void get_purchase_detail_success() throws Exception {
                        PurchaseOrder purchaseOrder = purchaseOrderTestDataFactory.createPurchaseOrder();

                        mockMvc.perform(get("/api/purchase-order/{purchaseOrderId}", purchaseOrder.getId())
                                        .with(authentication(auth)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.header.partnerName").value("SUP"))
                                        .andExpect(jsonPath("$.data.lines[0].itemCode").value("CODE"))
                                        .andDo(print());
                }
        }
}
