package com.erp.mini.partner.controller;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.common.response.PageResponse;
import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.partner.dto.AddPartnerRequest;
import com.erp.mini.partner.dto.SearchPartnerCondition;
import com.erp.mini.partner.dto.SearchPartnerResponse;
import com.erp.mini.partner.dto.UpdatePartnerRequest;
import com.erp.mini.partner.service.PartnerService;
import com.erp.mini.util.CustomMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PartnerController.class)
@AutoConfigureMockMvc(addFilters = false)
class PartnerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PartnerService partnerService;

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class add_partner_test {
        @Test
        void add_partner_success() throws Exception {
            AddPartnerRequest request = new AddPartnerRequest(
                    "기성식품", PartnerType.SUPPLIER, null, null
            );

            mockMvc.perform(post("/api/partner")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andDo(print());
        }

        @Test
        void add_partner_fail_with_missing_fields() throws Exception {
            AddPartnerRequest request = new AddPartnerRequest(
                    "기성식품", null, null, null
            );

            mockMvc.perform(post("/api/partner")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @CustomMockUser(id = 1L, employeeNumber = "EMP001")
    class update_partner_test {
        @Test
        void update_partner_success() throws Exception {
            UpdatePartnerRequest request = new UpdatePartnerRequest(null, "");

            mockMvc.perform(patch("/api/partner/{partnerId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void update_partner_fail_with_not_found() throws Exception {
            UpdatePartnerRequest request = new UpdatePartnerRequest(null, "");

            doThrow(new BusinessException(ErrorCode.NOT_FOUND))
                    .when(partnerService).updatePartner(anyLong(), any(UpdatePartnerRequest.class));

            mockMvc.perform(patch("/api/partner/{partnerId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    class search_partner_test {
        @Test
        void search_partner_success() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);

            List<SearchPartnerResponse> contents = List.of(
                    new SearchPartnerResponse(1L, "식품1", "CUS1", PartnerType.CUSTOMER),
                    new SearchPartnerResponse(2L, "식품2", "CUS2", PartnerType.CUSTOMER),
                    new SearchPartnerResponse(3L, "식품3", "CUS3", PartnerType.CUSTOMER),
                    new SearchPartnerResponse(4L, "식품4", "SUP1", PartnerType.SUPPLIER),
                    new SearchPartnerResponse(5L, "식품5", "SUP2", PartnerType.SUPPLIER)
            );

            PageImpl<SearchPartnerResponse> searchPartnerResponses = new PageImpl<>(contents, pageable, contents.size());
            PageResponse<SearchPartnerResponse> response = PageResponse.from(searchPartnerResponses);

            given(partnerService.searchPartners(any(SearchPartnerCondition.class), any())).willReturn(response);

            mockMvc.perform(get("/api/partner")
                            .param("keyword", "식품"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.pageInfo.page").value(1))
                    .andExpect(jsonPath("$.data.pageInfo.size").value(10))
                    .andExpect(jsonPath("$.data.pageInfo.totalElements").value(contents.size()))
                    .andDo(print());
        }
    }
}