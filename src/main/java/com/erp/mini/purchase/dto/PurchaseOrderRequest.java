package com.erp.mini.purchase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "구매 등록 요청")
public record PurchaseOrderRequest(
        @Schema(description = "거래처 식별자 ID", example = "1")
        @NotNull(message = "거래처 식별자 ID는 필수입니다.")
        Long partnerId,

        @Schema(description = "구매 목록")
        @NotEmpty(message = "최소 하나 이상의 항목은 필수입니다.")
        @Valid
        List<PurchaseLine> purchaseLines
) {
    public record PurchaseLine(
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
            @NotNull(message = "수량은 필수입니다.")
            @Min(value = 1, message = "최소 1 이상입니다.")
            Long qty
    ) {
    }
}