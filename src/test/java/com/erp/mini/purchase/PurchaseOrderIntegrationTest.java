package com.erp.mini.purchase;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private Authentication auth;

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
            PurchaseOrderRequest request = new PurchaseOrderRequest(
                    1L,
                    List.of(
                            new PurchaseOrderRequest.PurchaseLine(
                                    1L, 1L, BigDecimal.valueOf(1500), 10
                            ),
                            new PurchaseOrderRequest.PurchaseLine(
                                    2L, 1L, BigDecimal.valueOf(2500), 10
                            )
                    )
            );

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
            PurchaseOrderRequest request = new PurchaseOrderRequest(
                    1L,
                    List.of(
                            new PurchaseOrderRequest.PurchaseLine(
                                    1L, 1L, BigDecimal.valueOf(1500), 10
                            ),
                            new PurchaseOrderRequest.PurchaseLine(
                                    null, 1L, BigDecimal.valueOf(2500), 10
                            )
                    )
            );

            mockMvc.perform(post("/api/purchase-order")
                            .with(authentication(auth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void create_purchase_order_fail_with_warehouse_is_inactive() throws Exception {
            PurchaseOrderRequest request = new PurchaseOrderRequest(
                    1L,
                    List.of(
                            new PurchaseOrderRequest.PurchaseLine(
                                    1L, 1L, BigDecimal.valueOf(1500), 10
                            ),
                            new PurchaseOrderRequest.PurchaseLine(
                                    2L, 2L, BigDecimal.valueOf(2500), 10
                            )
                    )
            );

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

            AddPurchaseOrderLineRequest request = new AddPurchaseOrderLineRequest(
                    2L, 1L, BigDecimal.valueOf(2500), 10
            );

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

            AddPurchaseOrderLineRequest request = new AddPurchaseOrderLineRequest(
                    2L, null, BigDecimal.valueOf(2500), 10
            );

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

            AddPurchaseOrderLineRequest request = new AddPurchaseOrderLineRequest(
                    2L, 2L, BigDecimal.valueOf(2500), 10
            );

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
                    .andExpect(jsonPath("$.data.header.partnerCode").value("SUPPLIER_TEST"))
                    .andExpect(jsonPath("$.data.lines[0].itemCode").value("ITEM_TEST1"))
                    .andDo(print());
        }
    }
}
