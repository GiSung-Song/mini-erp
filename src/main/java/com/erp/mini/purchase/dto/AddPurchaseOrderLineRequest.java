package com.erp.mini.purchase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "구매 항목 추가 요청")
public record AddPurchaseOrderLineRequest(
        @Schema(description = "상품 식별자 ID", example = "1")
        @NotNull(message = "상품 식별자 ID는 필수입니다.")
        Long itemId,

        @Schema(description = "창고 식별자 ID", example = "1")
        @NotNull(message = "창고 식별자 ID는 필수입니다.")
        Long warehouseId,

        @Schema(description = "구매 가격", example = "15000.00")
        @NotNull(message = "구매 가격은 필수입니다.")
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 13, fraction = 2)
        BigDecimal unitCost,

        @Schema(description = "수량", example = "100")
        @Min(value = 1, message = "최소 1 이상입니다.")
        long qty
) {
}