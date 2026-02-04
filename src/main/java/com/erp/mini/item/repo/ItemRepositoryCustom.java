package com.erp.mini.item.repo;

import com.erp.mini.item.dto.ItemDetailDto;
import com.erp.mini.item.dto.SearchItemCondition;
import com.erp.mini.item.dto.SearchItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemRepositoryCustom {
    Page<SearchItemResponse> search(SearchItemCondition searchItemCondition, Pageable pageable);
    List<ItemDetailDto> getItemDetail(Long itemId);
}