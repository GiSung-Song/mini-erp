package com.erp.mini.purchase.controller;

import com.erp.mini.purchase.domain.PurchaseStatus;
import com.erp.mini.purchase.dto.*;
import com.erp.mini.purchase.service.PurchaseOrderService;
import com.erp.mini.util.CustomMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PurchaseOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class PurchaseOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PurchaseOrderService purchaseOrderService;

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class create_purchase_order_test {
        @Test
        void create_purchase_success() throws Exception {
            PurchaseOrderRequest request = new PurchaseOrderRequest(
                    1L,
                    List.of(
                            new PurchaseOrderRequest.PurchaseLine(
                                    1L, 1L, BigDecimal.valueOf(1000), 5L)
                    )
            );

            mockMvc.perform(post("/api/purchase-order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andDo(print());
        }

        @Test
        void create_purchase_fail_with_missing_fields() throws Exception {
            PurchaseOrderRequest request = new PurchaseOrderRequest(
                    1L,
                    List.of(
                            new PurchaseOrderRequest.PurchaseLine(
                                    1L, null, BigDecimal.valueOf(1000), 5L)
                    )
            );

            mockMvc.perform(post("/api/purchase-order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class add_purchase_order_test {
        @Test
        void add_purchase_order_success() throws Exception {
            AddPurchaseOrderLineRequest request = new AddPurchaseOrderLineRequest(
                    1L, 1L, BigDecimal.valueOf(1000), 5L
            );

            mockMvc.perform(post("/api/purchase-order/{purchaseOrderId}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void add_purchase_order_fail_with_missing_fields() throws Exception {
            AddPurchaseOrderLineRequest request = new AddPurchaseOrderLineRequest(
                    null, 1L, BigDecimal.valueOf(1000), 5L
            );

            mockMvc.perform(post("/api/purchase-order/{purchaseOrderId}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void add_purchase_order_fail_with_invalid_path_variable() throws Exception {
            AddPurchaseOrderLineRequest request = new AddPurchaseOrderLineRequest(
                    1L, 1L, BigDecimal.valueOf(1000), 5L
            );

            mockMvc.perform(post("/api/purchase-order/{purchaseOrderId}", "43214231L")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class remove_purchase_order_test {
        @Test
        void remove_purchase_order_success() throws Exception {
            mockMvc.perform(delete("/api/purchase-order/{purchaseOrderId}/line/{purchaseOrderLineId}", 1, 1))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void remove_purchase_order_fail_with_invalid_path_variable() throws Exception {
            mockMvc.perform(delete("/api/purchase-order/{purchaseOrderId}/line/{purchaseOrderLineId}", 1, "4321L"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class order_purchase_test {
        @Test
        void order_purchase_success() throws Exception {
            mockMvc.perform(patch("/api/purchase-order/{purchaseOrderId}/order", 1))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void order_purchase_fail_with_invalid_path_variable() throws Exception {
            mockMvc.perform(patch("/api/purchase-order/{purchaseOrderId}/order", "4321L"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class cancel_purchase_test {
        @Test
        void cancel_purchase_success() throws Exception {
            mockMvc.perform(patch("/api/purchase-order/{purchaseOrderId}/cancel", 1))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void cancel_purchase_fail_with_invalid_path_variable() throws Exception {
            mockMvc.perform(patch("/api/purchase-order/{purchaseOrderId}/cancel", "43214321L"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class get_purchase_detail_test {
        @Test
        void get_purchase_detail_success() throws Exception {
            PurchaseDetailResponse response = new PurchaseDetailResponse(
                    new PurchaseHeaderDto(
                            1L, "C1", "파트너", PurchaseStatus.CREATED,
                            LocalDateTime.now(), LocalDateTime.now(), "관리자", "관리자"
                    ),
                    List.of(
                            new PurchaseLineDto(
                                    1L, "IC1", "아이템", "WH1", "창고",
                                    "어딘가", 10, BigDecimal.valueOf(1000), BigDecimal.valueOf(10000)
                            )
                    )
            );

            given(purchaseOrderService.getPurchaseOrderDetail(anyLong())).willReturn(response);

            mockMvc.perform(get("/api/purchase-order/{purchaseOrderId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.header.partnerCode").value("C1"))
                    .andExpect(jsonPath("$.data.lines[0].itemCode").value("IC1"))
                    .andDo(print());
        }

        @Test
        void get_purchase_detail_fail_with_invalid_path_variable() throws Exception {
            mockMvc.perform(get("/api/purchase-order/{purchaseOrderId}", "4321L"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}