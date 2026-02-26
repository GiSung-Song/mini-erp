package com.erp.mini.inventory.controller;

import com.erp.mini.common.response.CustomResponse;
import com.erp.mini.common.response.PageResponse;
import com.erp.mini.inventory.domain.TransactionType;
import com.erp.mini.inventory.dto.ItxDetailResponse;
import com.erp.mini.inventory.dto.ItxSearchCondition;
import com.erp.mini.inventory.dto.ItxSearchDto;
import com.erp.mini.inventory.service.InventoryTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory/transaction")
@Tag(name = "InventoryTransaction", description = "재고 이력 API")
public class InventoryTransactionController {

    private final InventoryTransactionService inventoryTransactionService;

    @Operation(summary = "재고 이력 검색", description = "재고 이력을 검색한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping
    public ResponseEntity<CustomResponse<PageResponse<ItxSearchDto>>> searchInventoryTransaction(
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        ItxSearchCondition condition = new ItxSearchCondition(itemId, warehouseId, startDate, endDate, type);

        PageResponse<ItxSearchDto> response = inventoryTransactionService.getInventoryTransaction(condition, pageable);

        return CustomResponse.ok(response);
    }

    @Operation(summary = "재고 이력 상세 조회", description = "재고 이력을 상세 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/{itxId}")
    public ResponseEntity<CustomResponse<ItxDetailResponse>> searchInventoryTransactionDetail(
            @PathVariable Long itxId
    ) {
        ItxDetailResponse response = inventoryTransactionService.getInventoryTransactionDetail(itxId);
        return CustomResponse.ok(response);
    }
}