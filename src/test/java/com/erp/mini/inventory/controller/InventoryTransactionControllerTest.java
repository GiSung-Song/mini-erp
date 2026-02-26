package com.erp.mini.inventory.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.erp.mini.common.response.PageResponse;
import com.erp.mini.inventory.domain.RefType;
import com.erp.mini.inventory.domain.TransactionType;
import com.erp.mini.inventory.dto.ItxDetailResponse;
import com.erp.mini.inventory.dto.ItxSearchCondition;
import com.erp.mini.inventory.dto.ItxSearchDto;
import com.erp.mini.inventory.service.InventoryTransactionService;
import com.erp.mini.util.CustomMockUser;

@WebMvcTest(controllers = InventoryTransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryTransactionService inventoryTransactionService;

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class search_itx_test {
        @Test
        void search_success_no_filter() throws Exception {
            ItxSearchDto dto1 = new ItxSearchDto(1L, "IC1", "Item1", "WH1", "Warehouse1",
                    TransactionType.INBOUND, 10L, LocalDate.now().atStartOfDay(), "user1");
            ItxSearchDto dto2 = new ItxSearchDto(2L, "IC2", "Item2", "WH2", "Warehouse2",
                    TransactionType.OUTBOUND, -5L, LocalDate.now().atStartOfDay(), "user2");

            // create a simple page
            Page<ItxSearchDto> page = new PageImpl<>(List.of(dto1, dto2));
            PageResponse<ItxSearchDto> resp = PageResponse.from(page);

            given(inventoryTransactionService.getInventoryTransaction(any(ItxSearchCondition.class), any()))
                    .willReturn(resp);

            mockMvc.perform(get("/api/inventory/transaction")
                    .param("page", "0")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].itemCode").value("IC1"))
                    .andDo(print());
        }

        @Test
        void search_success_with_filters() throws Exception {
            ItxSearchDto dto = new ItxSearchDto(1L, "IC1", "Item1", "WH1", "Warehouse1",
                    TransactionType.INBOUND, 10L, LocalDate.now().atStartOfDay(), "user1");
            Page<ItxSearchDto> page = new PageImpl<>(List.of(dto));
            given(inventoryTransactionService.getInventoryTransaction(any(ItxSearchCondition.class), any()))
                    .willReturn(PageResponse.from(page));

            mockMvc.perform(get("/api/inventory/transaction")
                    .param("itemId", "1")
                    .param("type", "INBOUND")
                    .param("page", "0")
                    .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].itemCode").value("IC1"))
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class detail_itx_test {
        @Test
        void detail_success() throws Exception {
            ItxDetailResponse detail = new ItxDetailResponse(1L, "IC1", "Item1", "WH1", "Warehouse1",
                    TransactionType.INBOUND, 10L, LocalDate.now().atStartOfDay(), "user1", 100L,
                    RefType.PURCHASE_ORDER, "", "SUP1", "Supplier");
            given(inventoryTransactionService.getInventoryTransactionDetail(anyLong()))
                    .willReturn(detail);

            mockMvc.perform(get("/api/inventory/transaction/{itxId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.refType").value("PURCHASE_ORDER"))
                    .andDo(print());
        }

        @Test
        void detail_fail_with_invalid_path_variable() throws Exception {
            mockMvc.perform(get("/api/inventory/transaction/{itxId}", "notLong"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}
