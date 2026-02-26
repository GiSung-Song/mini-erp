package com.erp.mini.sales.controller;

import com.erp.mini.common.response.CustomResponse;
import com.erp.mini.sales.dto.AddSalesOrderLineRequest;
import com.erp.mini.sales.dto.SalesDetailResponse;
import com.erp.mini.sales.dto.SalesOrderRequest;
import com.erp.mini.sales.service.SalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sales-order")
@Tag(name = "SalesOrder", description = "판매 발주 API")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    //출고 생성
    @Operation(summary = "판매 발주 생성", description = "판매 발주를 생성한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공")
    })
    @PostMapping
    public ResponseEntity<CustomResponse<Void>> createSalesOrder(
            @RequestBody @Valid SalesOrderRequest request
    ) {
        salesOrderService.createSale(request);

        return CustomResponse.created();
    }

    //출고 항목 추가
    @Operation(summary = "판매 발주 추가", description = "판매 발주를 추가한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추가 성공")
    })
    @PostMapping("/{salesOrderId}")
    public ResponseEntity<CustomResponse<Void>> addSalesOrder(
            @PathVariable Long salesOrderId,
            @RequestBody @Valid AddSalesOrderLineRequest request
    ) {
        salesOrderService.addOrderLine(salesOrderId, request);

        return CustomResponse.ok();
    }

    //출고 항목 삭제
    @Operation(summary = "판매 발주 항목 삭제", description = "판매 발주 항목을 삭제한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공")
    })
    @DeleteMapping("/{salesOrderId}/line/{salesOrderLineId}")
    public ResponseEntity<CustomResponse<Void>> removeSalesOrder(
            @PathVariable Long salesOrderId,
            @PathVariable Long salesOrderLineId
    ) {
        salesOrderService.removeSaleOrder(salesOrderId, salesOrderLineId);

        return CustomResponse.ok();
    }

    //출고 주문
    @Operation(summary = "판매 주문 확정", description = "판매 주문을 확정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 확정")
    })
    @PatchMapping("/{salesOrderId}/order")
    public ResponseEntity<CustomResponse<Void>> orderSales(
            @PathVariable Long salesOrderId
    ) {
        salesOrderService.orderSales(salesOrderId);

        return CustomResponse.ok();
    }

    //출고 취소
    @Operation(summary = "판매 주문 취소", description = "판매 주문을 취소한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "판매 취소")
    })
    @PatchMapping("/{salesOrderId}/cancel")
    public ResponseEntity<CustomResponse<Void>> cancelSales(
            @PathVariable Long salesOrderId
    ) {
        salesOrderService.cancelSales(salesOrderId);

        return CustomResponse.ok();
    }

    //상세 조회
    @Operation(summary = "판매 주문 상세 조회", description = "판매 주문을 상세 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/{salesOrderId}")
    public ResponseEntity<CustomResponse<SalesDetailResponse>> getSalesOrderDetail(
            @PathVariable Long salesOrderId
    ) {
        SalesDetailResponse response = salesOrderService.getSalesOrderDetail(salesOrderId);

        return CustomResponse.ok(response);
    }

    //출고 완료
    @Operation(summary = "판매 주문 배송 완료", description = "판매 주문을 배송 완료한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "배송 완료")
    })
    @PatchMapping("/{salesOrderId}/ship")
    public ResponseEntity<CustomResponse<Void>> shipSales(
            @PathVariable Long salesOrderId
    ) {
        salesOrderService.shipped(salesOrderId);

        return CustomResponse.ok();
    }
}
