package com.erp.mini.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "상품 가격 수정 요청")
public record ChangeItemPriceRequest(

        @NotNull(message = "상품 가격은 필수입니다.")
        @Schema(description = "상품 가격", example = "100000")
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 13, fraction = 2)
        BigDecimal basePrice
) {
}
