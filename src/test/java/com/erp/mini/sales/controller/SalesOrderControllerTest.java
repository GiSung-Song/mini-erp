package com.erp.mini.sales.controller;

import com.erp.mini.sales.dto.AddSalesOrderLineRequest;
import com.erp.mini.sales.dto.SalesDetailResponse;
import com.erp.mini.sales.dto.SalesHeaderDto;
import com.erp.mini.sales.dto.SalesLineDto;
import com.erp.mini.sales.dto.SalesOrderRequest;
import com.erp.mini.sales.service.SalesOrderService;
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

@WebMvcTest(controllers = SalesOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class SalesOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SalesOrderService salesOrderService;

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class create_sales_test {
        @Test
        void create_sales_success() throws Exception {
            SalesOrderRequest request = new SalesOrderRequest(
                    1L,
                    "구매자",
                    "010-1234-5678",
                    "12345",
                    "주소1",
                    "주소2",
                    List.of(new SalesOrderRequest.SaleLine(1L, 1L, BigDecimal.valueOf(1500), 5L))
            );

            mockMvc.perform(post("/api/sales-order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andDo(print());
        }

        @Test
        void create_sales_fail_with_missing_fields() throws Exception {
            SalesOrderRequest request = new SalesOrderRequest(
                    null,
                    "",
                    "",
                    "",
                    "",
                    "",
                    List.of()
            );

            mockMvc.perform(post("/api/sales-order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class add_sales_line_test {
        @Test
        void add_sales_line_success() throws Exception {
            AddSalesOrderLineRequest request = new AddSalesOrderLineRequest(1L, 1L, BigDecimal.valueOf(1500), 5L);

            mockMvc.perform(post("/api/sales-order/{salesOrderId}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void add_sales_line_fail_with_missing_fields() throws Exception {
            AddSalesOrderLineRequest request = new AddSalesOrderLineRequest(null, 1L, BigDecimal.valueOf(1500), 5L);

            mockMvc.perform(post("/api/sales-order/{salesOrderId}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void add_sales_line_fail_with_invalid_path_variable() throws Exception {
            AddSalesOrderLineRequest request = new AddSalesOrderLineRequest(1L, 1L, BigDecimal.valueOf(1500), 5L);

            mockMvc.perform(post("/api/sales-order/{salesOrderId}", "notLong")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class remove_sales_line_test {
        @Test
        void remove_sales_line_success() throws Exception {
            mockMvc.perform(delete("/api/sales-order/{salesOrderId}/line/{salesOrderLineId}", 1, 1))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void remove_sales_line_fail_with_invalid_path_variable() throws Exception {
            mockMvc.perform(delete("/api/sales-order/{salesOrderId}/line/{salesOrderLineId}", 1, "notLong"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class order_sales_test {
        @Test
        void order_sales_success() throws Exception {
            mockMvc.perform(patch("/api/sales-order/{salesOrderId}/order", 1))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void order_sales_fail_with_invalid_path_variable() throws Exception {
            mockMvc.perform(patch("/api/sales-order/{salesOrderId}/order", "notLong"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class cancel_sales_test {
        @Test
        void cancel_sales_success() throws Exception {
            mockMvc.perform(patch("/api/sales-order/{salesOrderId}/cancel", 1))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void cancel_sales_fail_with_invalid_path_variable() throws Exception {
            mockMvc.perform(patch("/api/sales-order/{salesOrderId}/cancel", "notLong"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class get_sales_detail_test {
        @Test
        void get_sales_detail_success() throws Exception {
            SalesHeaderDto header = new SalesHeaderDto(
                    1L, "P1", "파트너", "구매자", "010-1234-5678",
                    "12345", "주소1", "주소2", null,
                    LocalDateTime.now(), LocalDateTime.now(), "admin", "admin"
            );

            SalesLineDto line = new SalesLineDto(1L, "IC1", "아이템", "WH1", "창고", "어딘가", 10L, BigDecimal.valueOf(1000), BigDecimal.valueOf(10000));

            SalesDetailResponse response = new SalesDetailResponse(header, List.of(line));

            given(salesOrderService.getSalesOrderDetail(anyLong())).willReturn(response);

            mockMvc.perform(get("/api/sales-order/{salesOrderId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.header.partnerCode").value("P1"))
                    .andExpect(jsonPath("$.data.lines[0].itemCode").value("IC1"))
                    .andDo(print());
        }

        @Test
        void get_sales_detail_fail_with_invalid_path_variable() throws Exception {
            mockMvc.perform(get("/api/sales-order/{salesOrderId}", "notLong"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class ship_sales_test {
        @Test
        void ship_sales_success() throws Exception {
            mockMvc.perform(patch("/api/sales-order/{salesOrderId}/ship", 1))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void ship_sales_fail_with_invalid_path_variable() throws Exception {
            mockMvc.perform(patch("/api/sales-order/{salesOrderId}/ship", "notLong"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}
