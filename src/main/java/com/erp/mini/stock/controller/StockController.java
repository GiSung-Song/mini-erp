package com.erp.mini.stock.controller;

import com.erp.mini.common.response.CustomResponse;
import com.erp.mini.stock.dto.AdjustStockRequest;
import com.erp.mini.stock.dto.ItemStockResponse;
import com.erp.mini.stock.dto.WarehouseStockResponse;
import com.erp.mini.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    // 재고 조정
    @Operation(summary = "재고 조정", description = "재고를 조정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조정 성공")
    })
    @PatchMapping("/adjust")
    public ResponseEntity<CustomResponse<Void>> adjustStock(@Valid @RequestBody AdjustStockRequest request) {
        stockService.adjust(request);

        return CustomResponse.ok();
    }

    // 특정 상품 재고 조회
    @Operation(summary = "특정 상품 재고 조회", description = "특정 상품의 재고 현황을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/item/{itemId}")
    public ResponseEntity<CustomResponse<ItemStockResponse>> getItemStock(
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        ItemStockResponse response = stockService.getItemStock(itemId, pageable);

        return CustomResponse.ok(response);
    }

    // 창고별 재고 조회
    @Operation(summary = "특정 창고 재고 조회", description = "특정 창고의 재고 현황을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<CustomResponse<WarehouseStockResponse>> getWarehouseStock(
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        WarehouseStockResponse response = stockService.getWarehouseStock(warehouseId, pageable);

        return CustomResponse.ok(response);
    }
}
