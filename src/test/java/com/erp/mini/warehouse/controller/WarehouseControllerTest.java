package com.erp.mini.warehouse.controller;

import com.erp.mini.common.response.PageResponse;
import com.erp.mini.util.CustomMockUser;
import com.erp.mini.warehouse.domain.WarehouseStatus;
import com.erp.mini.warehouse.dto.AddWarehouseRequest;
import com.erp.mini.warehouse.dto.SearchWarehouseCondition;
import com.erp.mini.warehouse.dto.SearchWarehouseResponse;
import com.erp.mini.warehouse.service.WarehouseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WarehouseController.class)
@AutoConfigureMockMvc(addFilters = false)
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WarehouseService warehouseService;

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class add_warehouse_test {
        @Test
        void add_warehouse_success() throws Exception {
            AddWarehouseRequest request = new AddWarehouseRequest(
                    "서울 창고", "어딘가", WarehouseStatus.ACTIVE
            );

            mockMvc.perform(post("/api/warehouse")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andDo(print());
        }

        @Test
        void add_warehouse_fail_with_missing_fields() throws Exception {
            AddWarehouseRequest request = new AddWarehouseRequest(
                    "서울 창고", null, WarehouseStatus.ACTIVE
            );

            mockMvc.perform(post("/api/warehouse")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class deactivate_warehouse_test {
        @Test
        void deactivate_warehouse_success() throws Exception {
            mockMvc.perform(patch("/api/warehouse/{warehouseId}/deactivate", 1))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void deactivate_warehouse_fail_with_invalid_path_variable() throws Exception {
            mockMvc.perform(patch("/api/warehouse/{warehouseId}/deactivate", "warehouseId"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class activate_warehouse_test {
        @Test
        void activate_warehouse_success() throws Exception {
            mockMvc.perform(patch("/api/warehouse/{warehouseId}/activate", 1))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void activate_warehouse_fail_with_invalid_path_variable() throws Exception {
            mockMvc.perform(patch("/api/warehouse/{warehouseId}/activate", "warehouseId"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class search_warehouse_test {
        @Test
        void search_warehouse_success() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);

            List<SearchWarehouseResponse> contents = List.of(
                    new SearchWarehouseResponse(1L, "창고1", "WH000001", "어딘가1", WarehouseStatus.ACTIVE),
                    new SearchWarehouseResponse(2L, "창고2", "WH000002", "어딘가2", WarehouseStatus.ACTIVE),
                    new SearchWarehouseResponse(3L, "창고3", "WH000003", "어딘가3", WarehouseStatus.ACTIVE),
                    new SearchWarehouseResponse(4L, "창고4", "WH000004", "어딘가4", WarehouseStatus.INACTIVE),
                    new SearchWarehouseResponse(5L, "창고5", "WH000005", "어딘가5", WarehouseStatus.ACTIVE)
            );

            Page<SearchWarehouseResponse> page = new PageImpl<>(contents, pageable, contents.size());
            PageResponse<SearchWarehouseResponse> response = PageResponse.from(page);

            given(warehouseService.searchWarehouse(any(SearchWarehouseCondition.class), any())).willReturn(response);

            mockMvc.perform(get("/api/warehouse")
                            .param("keyword", "창고"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.pageInfo.page").value(1))
                    .andExpect(jsonPath("$.data.pageInfo.size").value(10))
                    .andExpect(jsonPath("$.data.pageInfo.totalElements").value(contents.size()))
                    .andDo(print());
        }
    }
}