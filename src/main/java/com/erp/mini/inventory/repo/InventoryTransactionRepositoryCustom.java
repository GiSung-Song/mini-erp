package com.erp.mini.inventory.repo;

import com.erp.mini.inventory.dto.ItxDetailResponse;
import com.erp.mini.inventory.dto.ItxSearchCondition;
import com.erp.mini.inventory.dto.ItxSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryTransactionRepositoryCustom {
    Page<ItxSearchDto> findInventoryTransaction(ItxSearchCondition condition, Pageable pageable);
    ItxDetailResponse findInventoryTransactionDetail(Long inventoryTransactionId);
}
