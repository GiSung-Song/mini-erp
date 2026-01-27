package com.erp.mini.item.dto;

import com.erp.mini.item.domain.ItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemAddRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        String name,

        @NotBlank(message = "상품 코드는 필수입니다.")
        String code,

        @NotBlank(message = "상품 가격은 필수입니다.")
        String basePrice,

        @NotNull(message = "상품 상태는 필수입니다.")
        ItemStatus itemStatus
) {
}
