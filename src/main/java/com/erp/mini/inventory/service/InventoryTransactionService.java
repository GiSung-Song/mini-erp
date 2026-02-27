package com.erp.mini.inventory.service;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.common.response.PageResponse;
import com.erp.mini.inventory.dto.ItxDetailResponse;
import com.erp.mini.inventory.dto.ItxSearchCondition;
import com.erp.mini.inventory.dto.ItxSearchDto;
import com.erp.mini.inventory.repo.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryTransactionService {

    private final InventoryTransactionRepository inventoryTransactionRepository;

    // 재고 이력 리스트 조회
    @Transactional(readOnly = true)
    public PageResponse<ItxSearchDto> getInventoryTransaction(ItxSearchCondition condition, Pageable pageable) {
        Page<ItxSearchDto> pages
                = inventoryTransactionRepository.findInventoryTransaction(condition, pageable);

        return PageResponse.from(pages);
    }

    @Transactional(readOnly = true)
    public ItxDetailResponse getInventoryTransactionDetail(Long itxId) {
        ItxDetailResponse response = inventoryTransactionRepository.findInventoryTransactionDetail(itxId);

        if (response == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "해당 재고 이력이 존재하지 않습니다.");
        }

        return response;
    }
}