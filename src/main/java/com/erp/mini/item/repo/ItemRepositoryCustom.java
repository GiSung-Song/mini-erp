package com.erp.mini.item.repo;

import com.erp.mini.item.dto.SearchItemCondition;
import com.erp.mini.item.dto.SearchItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemRepositoryCustom {
    Page<SearchItemResponse> search(SearchItemCondition searchItemCondition, Pageable pageable);
}