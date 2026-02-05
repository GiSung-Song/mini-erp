package com.erp.mini.item.dto;

import com.erp.mini.item.domain.ItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "상품 등록 요청")
public record AddItemRequest(
        @Schema(description = "상품명", example = "설탕600g")
        @NotBlank(message = "상품명은 필수입니다.")
        String name,

        @NotNull(message = "상품 가격은 필수입니다.")
        @Schema(description = "상품 가격", example = "150000")
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 13, fraction = 2)
        BigDecimal basePrice,

        @Schema(description = "상품 상태", example = "ACTIVE")
        @NotNull(message = "상품 상태는 필수입니다.")
        ItemStatus itemStatus
) {
}
