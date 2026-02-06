package com.erp.mini.warehouse.dto;

import com.erp.mini.warehouse.domain.WarehouseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "창고 등록 요청")
public record AddWarehouseRequest(
        @Schema(description = "창고명", example = "서울 1창고")
        @NotBlank(message = "창고명은 필수입니다.")
        String name,

        @Schema(description = "위치", example = "서울시 울산구 부산동")
        @NotBlank(message = "위치는 필수입니다.")
        String location,

        @Schema(description = "창고 상태", example = "ACTIVE")
        @NotNull(message = "창고 상태는 필수입니다.")
        WarehouseStatus status
) {
}
