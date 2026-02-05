package com.erp.mini.partner.repo;

import com.erp.mini.partner.dto.SearchPartnerCondition;
import com.erp.mini.partner.dto.SearchPartnerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PartnerRepositoryCustom {
    Page<SearchPartnerResponse> search(SearchPartnerCondition condition, Pageable pageable);
}