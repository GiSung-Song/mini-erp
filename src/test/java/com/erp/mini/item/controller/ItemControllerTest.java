package com.erp.mini.item.controller;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.common.response.PageResponse;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.item.dto.AddItemRequest;
import com.erp.mini.item.dto.ChangeItemPriceRequest;
import com.erp.mini.item.dto.SearchItemCondition;
import com.erp.mini.item.dto.SearchItemResponse;
import com.erp.mini.item.service.ItemService;
import com.erp.mini.util.CustomMockUser;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemService itemService;

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class add_item_test {
        @Test
        void add_item_success() throws Exception {
            AddItemRequest request = new AddItemRequest(
                    "테스트 상품", BigDecimal.valueOf(15000), ItemStatus.ACTIVE
            );

            mockMvc.perform(post("/api/item")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andDo(print());
        }

        @Test
        void add_item_fail_missing_fields() throws Exception {
            AddItemRequest request = new AddItemRequest(
                    "테스트 상품", null, ItemStatus.ACTIVE
            );

            mockMvc.perform(post("/api/item")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class deactivate_item_test {
        @Test
        void deactivate_item_success() throws Exception {
            mockMvc.perform(delete("/api/item/{itemId}", 1))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void deactivate_item_fail_with_invalid_path_variable() throws Exception {
            mockMvc.perform(delete("/api/item/{itemId}", "itemId"))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void deactivate_item_fail_with_item_not_found() throws Exception {
            doThrow(new BusinessException(ErrorCode.NOT_FOUND)).when(itemService).deactivateItem(anyLong());

            mockMvc.perform(delete("/api/item/{itemId}", 1))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class search_item_test {
        @Test
        void search_item_success() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            List<SearchItemResponse> contents =
                    List.of(
                            new SearchItemResponse(1L, "ITEM - 1", "IC000001", ItemStatus.ACTIVE),
                            new SearchItemResponse(2L, "ITEM - 2", "IC000002", ItemStatus.ACTIVE),
                            new SearchItemResponse(3L, "ITEM - 3", "IC000003", ItemStatus.ACTIVE),
                            new SearchItemResponse(4L, "ITEM - 4", "IC000004", ItemStatus.INACTIVE),
                            new SearchItemResponse(5L, "ITEM - 5", "IC000005", ItemStatus.ACTIVE)
                    );

            Page<SearchItemResponse> data = new PageImpl<>(contents, pageable, contents.size());
            PageResponse<SearchItemResponse> response = PageResponse.from(data);

            given(itemService.getItemBySearch(any(SearchItemCondition.class), any(Pageable.class))).willReturn(response);

            mockMvc.perform(get("/api/item")
                            .param("code", "IC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.pageInfo.page").value(1))
                    .andExpect(jsonPath("$.data.pageInfo.size").value(10))
                    .andExpect(jsonPath("$.data.pageInfo.totalElements").value(contents.size()))
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class change_item_price_test {
        @Test
        void change_item_price_success() throws Exception {
            ChangeItemPriceRequest request = new ChangeItemPriceRequest(
                    BigDecimal.valueOf(10000)
            );

            mockMvc.perform(patch("/api/item/{itemId}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void change_item_price_fail_with_wrong_path_variable() throws Exception {
            ChangeItemPriceRequest request = new ChangeItemPriceRequest(
                    BigDecimal.valueOf(10000)
            );

            mockMvc.perform(patch("/api/item/{itemId}", "itemId")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void change_item_price_fail_with_missing_fields() throws Exception {
            ChangeItemPriceRequest request = new ChangeItemPriceRequest(null);

            mockMvc.perform(patch("/api/item/{itemId}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void change_item_price_fail_with_not_found() throws Exception {
            doThrow(new BusinessException(ErrorCode.NOT_FOUND))
                    .when(itemService).changePrice(anyLong(), any(ChangeItemPriceRequest.class));

            ChangeItemPriceRequest request = new ChangeItemPriceRequest(BigDecimal.valueOf(10000));

            mockMvc.perform(patch("/api/item/{itemId}", 1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }
}