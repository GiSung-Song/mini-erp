package com.erp.mini.warehouse.controller;

import com.erp.mini.common.response.CustomResponse;
import com.erp.mini.common.response.PageResponse;
import com.erp.mini.warehouse.domain.WarehouseStatus;
import com.erp.mini.warehouse.dto.AddWarehouseRequest;
import com.erp.mini.warehouse.dto.SearchWarehouseCondition;
import com.erp.mini.warehouse.dto.SearchWarehouseResponse;
import com.erp.mini.warehouse.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/warehouse")
@Tag(name = "Warehouse", description = "창고 API")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Operation(summary = "창고 등록", description = "창고를 등록한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공")
    })
    @PostMapping
    public ResponseEntity<CustomResponse<Void>> addWarehouse(
            @RequestBody @Valid AddWarehouseRequest request
    ) {
        warehouseService.addWarehouse(request);

        return CustomResponse.created();
    }

    @Operation(summary = "창고 비활성화", description = "창고를 비활성화한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비활성화 성공")
    })
    @PatchMapping("/{warehouseId}/deactivate")
    public ResponseEntity<CustomResponse<Void>> deactivateWarehouse(
            @PathVariable Long warehouseId
    ) {
        warehouseService.deactivateWarehouse(warehouseId);

        return CustomResponse.ok();
    }

    @Operation(summary = "창고 활성화", description = "창고를 활성화한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "활성화 성공")
    })
    @PatchMapping("/{warehouseId}/activate")
    public ResponseEntity<CustomResponse<Void>> activateWarehouse(
            @PathVariable Long warehouseId
    ) {
        warehouseService.activateWarehouse(warehouseId);

        return CustomResponse.ok();
    }

    @Operation(summary = "창고 검색", description = "창고 목록을 검색한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping
    public ResponseEntity<CustomResponse<PageResponse<SearchWarehouseResponse>>> searchPartners(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) WarehouseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        SearchWarehouseCondition condition = new SearchWarehouseCondition(keyword, status);
        Pageable pageable = PageRequest.of(page, size);

        PageResponse<SearchWarehouseResponse> response = warehouseService.searchWarehouse(condition, pageable);

        return CustomResponse.ok(response);
    }
}
