package com.erp.mini.partner.service;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.PageResponse;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.domain.PartnerFixture;
import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.partner.dto.SearchPartnerCondition;
import com.erp.mini.partner.dto.SearchPartnerResponse;
import com.erp.mini.partner.dto.UpdatePartnerRequest;
import com.erp.mini.partner.repo.PartnerRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PartnerServiceTest {

    @Mock
    private PartnerRepository partnerRepository;

    @InjectMocks
    private PartnerService partnerService;

    @Nested
    class update_partner_test {
        @Test
        void update_partner_success() {
            Partner partner = PartnerFixture.create(
                    "기성식품", "CUS000001", PartnerType.CUSTOMER,
                    "01012341234", "test@email.com"
            );

            given(partnerRepository.findById(1L)).willReturn(Optional.of(partner));

            UpdatePartnerRequest request = new UpdatePartnerRequest("", null);

            partnerService.updatePartner(1L, request);

            assertThat(partner.getEmail()).isNull();
            assertThat(partner.getPhone()).isEqualTo("01012341234");
        }

        @Test
        void update_partner_fail_with_not_found() {
            UpdatePartnerRequest request = new UpdatePartnerRequest("", null);

            given(partnerRepository.findById(1L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> partnerService.updatePartner(1L, request))
                    .satisfies(ex -> {
                        BusinessException exception = (BusinessException) ex;

                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class search_partners_test {
        @Test
        void search_partners_success() {
            Pageable pageable = PageRequest.of(0, 10);

            List<SearchPartnerResponse> contents = List.of(
                    new SearchPartnerResponse(1L, "식품1", "CUS1", PartnerType.CUSTOMER),
                    new SearchPartnerResponse(2L, "식품2", "CUS2", PartnerType.CUSTOMER),
                    new SearchPartnerResponse(3L, "식품3", "CUS3", PartnerType.CUSTOMER),
                    new SearchPartnerResponse(4L, "식품4", "SUP1", PartnerType.SUPPLIER),
                    new SearchPartnerResponse(5L, "식품5", "SUP2", PartnerType.SUPPLIER)
            );

            PageImpl<SearchPartnerResponse> searchPartnerResponses = new PageImpl<>(contents, pageable, contents.size());

            SearchPartnerCondition condition = new SearchPartnerCondition(null, null);

            given(partnerRepository.search(condition, pageable)).willReturn(searchPartnerResponses);

            PageResponse<SearchPartnerResponse> response = partnerService.searchPartners(condition, pageable);

            assertThat(response.pageInfo().page()).isEqualTo(1);
            assertThat(response.pageInfo().totalElements()).isEqualTo(contents.size());
            assertThat(response.pageInfo().size()).isEqualTo(10);
        }
    }
}