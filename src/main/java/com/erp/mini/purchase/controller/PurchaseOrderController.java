package com.erp.mini.purchase.controller;

import com.erp.mini.common.response.CustomResponse;
import com.erp.mini.purchase.dto.AddPurchaseOrderLineRequest;
import com.erp.mini.purchase.dto.PurchaseDetailResponse;
import com.erp.mini.purchase.dto.PurchaseOrderRequest;
import com.erp.mini.purchase.service.PurchaseOrderService;
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
@RequestMapping("/api/purchase-order")
@Tag(name = "PurchaseOrder", description = "구매 발주 API")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @Operation(summary = "구매 발주 생성", description = "구매 발주를 생성한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공")
    })
    @PostMapping
    public ResponseEntity<CustomResponse<Void>> createPurchaseOrder(
            @RequestBody @Valid PurchaseOrderRequest request
    ) {
        purchaseOrderService.createPurchase(request);

        return CustomResponse.created();
    }

    @Operation(summary = "구매 발주 추가", description = "구매 발주를 추가한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추가 성공")
    })
    @PostMapping("/{purchaseOrderId}")
    public ResponseEntity<CustomResponse<Void>> addPurchaseOrder(
            @PathVariable Long purchaseOrderId,
            @RequestBody @Valid AddPurchaseOrderLineRequest request
    ) {
        purchaseOrderService.addOrderLine(purchaseOrderId, request);

        return CustomResponse.ok();
    }

    @Operation(summary = "구매 발주 항목 삭제", description = "구매 발주 항목을 삭제한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공")
    })
    @DeleteMapping("/{purchaseOrderId}/line/{purchaseOrderLineId}")
    public ResponseEntity<CustomResponse<Void>> removePurchaseOrder(
            @PathVariable Long purchaseOrderId,
            @PathVariable Long purchaseOrderLineId
    ) {
        purchaseOrderService.removePurchaseOrder(purchaseOrderId, purchaseOrderLineId);

        return CustomResponse.ok();
    }

    @Operation(summary = "구매 주문 확정", description = "구매 주문을 확정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 확정")
    })
    @PatchMapping("/{purchaseOrderId}/order")
    public ResponseEntity<CustomResponse<Void>> orderPurchase(
            @PathVariable Long purchaseOrderId
    ) {
        purchaseOrderService.orderPurchase(purchaseOrderId);

        return CustomResponse.ok();
    }

    @Operation(summary = "구매 주문 취소", description = "구매 주문을 취소한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 취소")
    })
    @PatchMapping("/{purchaseOrderId}/cancel")
    public ResponseEntity<CustomResponse<Void>> cancelPurchase(
            @PathVariable Long purchaseOrderId
    ) {
        purchaseOrderService.cancelPurchase(purchaseOrderId);

        return CustomResponse.ok();
    }

    @Operation(summary = "구매 주문 수령 완료", description = "구매 주문을 수령 완료한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수령 완료")
    })
    @PatchMapping("/{purchaseOrderId}/receive")
    public ResponseEntity<CustomResponse<Void>> receivePurchase(
            @PathVariable Long purchaseOrderId
    ) {
        purchaseOrderService.receive(purchaseOrderId);

        return CustomResponse.ok();
    }

    @Operation(summary = "구매 주문 상세 조회", description = "구매 주문을 상세 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/{purchaseOrderId}")
    public ResponseEntity<CustomResponse<PurchaseDetailResponse>> getPurchaseOrderDetail(
            @PathVariable Long purchaseOrderId
    ) {
        PurchaseDetailResponse response = purchaseOrderService.getPurchaseOrderDetail(purchaseOrderId);

        return CustomResponse.ok(response);
    }
}
