package com.erp.mini.warehouse.service;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.common.response.PageResponse;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.dto.AddWarehouseRequest;
import com.erp.mini.warehouse.dto.SearchWarehouseCondition;
import com.erp.mini.warehouse.dto.SearchWarehouseResponse;
import com.erp.mini.warehouse.repo.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;

    // 등록
    @Transactional
    public void addWarehouse(AddWarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.save(
                Warehouse.createWarehouse(
                        request.name(), request.location(), request.status()
                )
        );

        warehouse.generateCode();
    }

    // 비활성화
    @Transactional
    public void deactivateWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 창고를 찾을 수 없습니다."));

        warehouse.deactivateWarehouse();
    }

    // 활성화
    @Transactional
    public void activateWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 창고를 찾을 수 없습니다."));

        warehouse.activateWarehouse();
    }

    // 검색
    @Transactional(readOnly = true)
    public PageResponse<SearchWarehouseResponse> searchWarehouse(
            SearchWarehouseCondition condition, Pageable pageable
    ) {
        Page<SearchWarehouseResponse> search = warehouseRepository.search(condition, pageable);

        return PageResponse.from(search);
    }
}
