package com.erp.mini.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "재고 조정 요청")
public record AdjustStockRequest(

        @Schema(description = "상품 식별자 ID", example = "1")
        @NotNull(message = "상품 식별자 ID는 필수입니다.")
        Long itemId,

        @Schema(description = "창고 식별자 ID", example = "1")
        @NotNull(message = "창고 식별자 ID는 필수입니다.")
        Long warehouseId,

        @Schema(description = "실제 수량", example = "100")
        @NotNull(message = "실제 수량은 필수입니다.")
        @Min(value = 1, message = "최소 1 이상입니다.")
        Long actualQty,

        @Schema(description = "사유", example = "재고 조사")
        @NotBlank(message = "사유는 필수입니다.")
        String reason
) {
}
