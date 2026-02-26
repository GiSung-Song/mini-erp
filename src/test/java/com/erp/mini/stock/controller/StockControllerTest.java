package com.erp.mini.stock.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.common.response.PageResponse;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.stock.dto.AdjustStockRequest;
import com.erp.mini.stock.dto.ItemInfoDto;
import com.erp.mini.stock.dto.ItemStockInfoDto;
import com.erp.mini.stock.dto.ItemStockResponse;
import com.erp.mini.stock.dto.WarehouseInfoDto;
import com.erp.mini.stock.dto.WarehouseStockInfoDto;
import com.erp.mini.stock.dto.WarehouseStockResponse;
import com.erp.mini.stock.service.StockService;
import com.erp.mini.util.CustomMockUser;
import com.erp.mini.warehouse.domain.WarehouseStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = StockController.class)
@AutoConfigureMockMvc(addFilters = false)
class StockControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private StockService stockService;

        // --- 재고 조정 테스트 ---
        @Nested
        @CustomMockUser(id = 1L, employeeNumber = "EMP001")
        class adjustStock_test {

                @Test
                void adjustStock_success() throws Exception {
                        AdjustStockRequest request = new AdjustStockRequest(1L, 1L, 100L, "stocktake");

                        mockMvc.perform(patch("/api/stock/adjust")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andDo(print());
                }

                @Test
                void adjustStock_fail_with_missing_itemId() throws Exception {
                        AdjustStockRequest request = new AdjustStockRequest(null, 1L, 100L, "stocktake");

                        mockMvc.perform(patch("/api/stock/adjust")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andDo(print());
                }

                @Test
                void adjustStock_fail_with_missing_warehouseId() throws Exception {
                        AdjustStockRequest request = new AdjustStockRequest(1L, null, 100L, "stocktake");

                        mockMvc.perform(patch("/api/stock/adjust")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andDo(print());
                }

                @Test
                void adjustStock_fail_with_missing_actualQty() throws Exception {
                        AdjustStockRequest request = new AdjustStockRequest(1L, 1L, null, "stocktake");

                        mockMvc.perform(patch("/api/stock/adjust")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andDo(print());
                }

                @Test
                void adjustStock_fail_with_invalid_actualQty_range() throws Exception {
                        // actualQty가 0 이하일 경우 검증 실패
                        AdjustStockRequest request = new AdjustStockRequest(1L, 1L, -100L, "stocktake");

                        mockMvc.perform(patch("/api/stock/adjust")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andDo(print());
                }

                @Test
                void adjustStock_fail_with_missing_reason() throws Exception {
                        AdjustStockRequest request = new AdjustStockRequest(1L, 1L, 100L, "");

                        mockMvc.perform(patch("/api/stock/adjust")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andDo(print());
                }

                @Test
                void adjustStock_fail_with_service_exception() throws Exception {
                        AdjustStockRequest request = new AdjustStockRequest(999L, 999L, 100L, "stocktake");

                        willThrow(new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."))
                                        .given(stockService).adjust(any(AdjustStockRequest.class));

                        mockMvc.perform(patch("/api/stock")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isNotFound())
                                        .andDo(print());
                }
        }

        // --- 상품별 재고 조회 테스트 ---
        @Nested
        @CustomMockUser(id = 1L, employeeNumber = "EMP001")
        class getItemStock_test {

                @Test
                void getItemStock_success() throws Exception {
                        ItemInfoDto itemInfo = new ItemInfoDto(1L, "상품A", "ITM001", null);
                        List<ItemStockInfoDto> stockInfos = List.of(
                                        new ItemStockInfoDto(1L, "창고A", "WH001", WarehouseStatus.ACTIVE, 50L),
                                        new ItemStockInfoDto(2L, "창고B", "WH002", WarehouseStatus.INACTIVE, 30L));

                        Page<ItemStockInfoDto> page = new PageImpl<>(stockInfos, PageRequest.of(0, 10),
                                        stockInfos.size());

                        PageResponse<ItemStockInfoDto> pages = PageResponse.from(page);
                        ItemStockResponse response = new ItemStockResponse(itemInfo, pages);

                        given(stockService.getItemStock(1L, PageRequest.of(0, 10))).willReturn(response);

                        mockMvc.perform(get("/api/stock/item/{itemId}", 1L)
                                        .param("page", "0")
                                        .param("size", "10"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.itemInfo.itemName").value("상품A"))
                                        .andExpect(jsonPath("$.data.itemStockInfos.pageInfo.totalElements").value(2))
                                        .andDo(print());
                }

                @Test
                void getItemStock_with_default_pagination() throws Exception {
                        ItemInfoDto itemInfo = new ItemInfoDto(1L, "상품B", "ITM002", null);
                        List<ItemStockInfoDto> stockInfos = List.of(
                                        new ItemStockInfoDto(1L, "창고A", "WH001", WarehouseStatus.ACTIVE, 50L),
                                        new ItemStockInfoDto(2L, "창고B", "WH002", WarehouseStatus.INACTIVE, 30L));

                        Page<ItemStockInfoDto> page = new PageImpl<>(stockInfos, PageRequest.of(0, 10),
                                        stockInfos.size());

                        PageResponse<ItemStockInfoDto> pages = PageResponse.from(page);
                        ItemStockResponse response = new ItemStockResponse(itemInfo, pages);

                        given(stockService.getItemStock(2L, PageRequest.of(0, 10))).willReturn(response);

                        mockMvc.perform(get("/api/stock/item/{itemId}", 2L))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.itemInfo.itemCode").value("ITM002"))
                                        .andDo(print());
                }

                @Test
                void getItemStock_with_custom_pagination() throws Exception {
                        ItemInfoDto itemInfo = new ItemInfoDto(3L, "상품C", "ITM003", null);
                        List<ItemStockInfoDto> stockInfos = List.of(
                                        new ItemStockInfoDto(1L, "창고A", "WH001", WarehouseStatus.ACTIVE, 50L),
                                        new ItemStockInfoDto(2L, "창고B", "WH002", WarehouseStatus.INACTIVE, 30L));

                        Page<ItemStockInfoDto> page = new PageImpl<>(stockInfos, PageRequest.of(1, 5),
                                        stockInfos.size());

                        PageResponse<ItemStockInfoDto> pages = PageResponse.from(page);
                        ItemStockResponse response = new ItemStockResponse(itemInfo, pages);

                        given(stockService.getItemStock(3L, PageRequest.of(1, 5))).willReturn(response);

                        mockMvc.perform(get("/api/stock/item/{itemId}", 3L)
                                        .param("page", "1")
                                        .param("size", "5"))
                                        .andExpect(status().isOk())
                                        .andDo(print());
                }

                @Test
                void getItemStock_fail_with_invalid_itemId() throws Exception {
                        mockMvc.perform(get("/api/stock/item/{itemId}", "invalidId"))
                                        .andExpect(status().isBadRequest())
                                        .andDo(print());
                }
        }

        // --- 창고별 재고 조회 테스트 ---
        @Nested
        @CustomMockUser(id = 1L, employeeNumber = "EMP001")
        class getWarehouseStock_test {

                @Test
                void getWarehouseStock_success() throws Exception {
                        WarehouseInfoDto whInfo = new WarehouseInfoDto(1L, "창고A", "WH001", "서울", null);
                        List<WarehouseStockInfoDto> stockInfos = List.of(
                                        new WarehouseStockInfoDto(1L, "상품1", "ITM001", BigDecimal.valueOf(1000),
                                                        ItemStatus.ACTIVE, 50L),
                                        new WarehouseStockInfoDto(2L, "상품2", "ITM002", BigDecimal.valueOf(2000),
                                                        ItemStatus.INACTIVE, 30L));
                        Page<WarehouseStockInfoDto> page = new PageImpl<>(stockInfos, PageRequest.of(0, 10),
                                        stockInfos.size());
                        PageResponse<WarehouseStockInfoDto> pages = PageResponse.from(page);
                        WarehouseStockResponse response = new WarehouseStockResponse(whInfo, pages);

                        given(stockService.getWarehouseStock(1L, PageRequest.of(0, 10))).willReturn(response);

                        mockMvc.perform(get("/api/stock/warehouse/{warehouseId}", 1L)
                                        .param("page", "0")
                                        .param("size", "10"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.warehouseInfo.warehouseName").value("창고A"))
                                        .andExpect(jsonPath("$.data.warehouseStockInfos.pageInfo.totalElements")
                                                        .value(2))
                                        .andExpect(jsonPath("$.data.warehouseStockInfos.content[0].itemName")
                                                        .value("상품1"))
                                        .andDo(print());
                }

                @Test
                void getWarehouseStock_with_default_pagination() throws Exception {
                        WarehouseInfoDto whInfo = new WarehouseInfoDto(2L, "창고B", "WH002", "부산", null);
                        List<WarehouseStockInfoDto> stockInfos = List.of(
                                        new WarehouseStockInfoDto(1L, "상품1", "ITM001", BigDecimal.valueOf(1000),
                                                        ItemStatus.ACTIVE, 50L),
                                        new WarehouseStockInfoDto(2L, "상품2", "ITM002", BigDecimal.valueOf(2000),
                                                        ItemStatus.INACTIVE, 30L));
                        Page<WarehouseStockInfoDto> page = new PageImpl<>(stockInfos, PageRequest.of(0, 10),
                                        stockInfos.size());
                        PageResponse<WarehouseStockInfoDto> pages = PageResponse.from(page);
                        WarehouseStockResponse response = new WarehouseStockResponse(whInfo, pages);

                        given(stockService.getWarehouseStock(2L, PageRequest.of(0, 10))).willReturn(response);

                        mockMvc.perform(get("/api/stock/warehouse/{warehouseId}", 2L))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.warehouseInfo.warehouseCode").value("WH002"))
                                        .andDo(print());
                }

                @Test
                void getWarehouseStock_with_custom_pagination() throws Exception {
                        WarehouseInfoDto whInfo = new WarehouseInfoDto(3L, "창고C", "WH003", "대구", null);
                        List<WarehouseStockInfoDto> stockInfos = List.of(
                                        new WarehouseStockInfoDto(1L, "상품1", "ITM001", BigDecimal.valueOf(1000),
                                                        ItemStatus.ACTIVE, 50L),
                                        new WarehouseStockInfoDto(2L, "상품2", "ITM002", BigDecimal.valueOf(2000),
                                                        ItemStatus.INACTIVE, 30L));
                        Page<WarehouseStockInfoDto> page = new PageImpl<>(stockInfos, PageRequest.of(2, 20),
                                        stockInfos.size());
                        PageResponse<WarehouseStockInfoDto> pages = PageResponse.from(page);
                        WarehouseStockResponse response = new WarehouseStockResponse(whInfo, pages);

                        given(stockService.getWarehouseStock(3L, PageRequest.of(2, 20))).willReturn(response);

                        mockMvc.perform(get("/api/stock/warehouse/{warehouseId}", 3L)
                                        .param("page", "2")
                                        .param("size", "20"))
                                        .andExpect(status().isOk())
                                        .andDo(print());
                }

                @Test
                void getWarehouseStock_fail_with_invalid_warehouseId() throws Exception {
                        mockMvc.perform(get("/api/stock/warehouse/{warehouseId}", "invalidId"))
                                        .andExpect(status().isBadRequest())
                                        .andDo(print());
                }
        }
}
