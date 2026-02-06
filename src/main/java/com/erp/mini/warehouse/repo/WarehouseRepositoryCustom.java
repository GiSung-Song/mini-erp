package com.erp.mini.warehouse.repo;

import com.erp.mini.warehouse.dto.SearchWarehouseCondition;
import com.erp.mini.warehouse.dto.SearchWarehouseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WarehouseRepositoryCustom {
    Page<SearchWarehouseResponse> search(SearchWarehouseCondition condition, Pageable pageable);
}
