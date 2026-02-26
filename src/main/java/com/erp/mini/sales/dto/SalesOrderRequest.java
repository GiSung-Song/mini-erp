package com.erp.mini.sales.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "판매 등록 요청")
public record SalesOrderRequest(
        @Schema(description = "거래처 식별자 ID", example = "1")
        @NotNull(message = "거래처 식별자 ID는 필수입니다.")
        Long partnerId,

        @Schema(description = "구매자명", example = "홍길동")
        @NotBlank(message = "구매자명은 필수입니다.")
        String customerName,

        @Schema(description = "구매자 휴대폰 번호", example = "01012345678")
        @NotBlank(message = "구매자 휴대폰 번호는 필수입니다.")
        String customerPhone,

        @Schema(description = "우편번호", example = "01234")
        @NotBlank(message = "우편번호는 필수입니다.")
        String zipcode,

        @Schema(description = "주소", example = "서울시 도봉구 방학로 200")
        @NotBlank(message = "주소는 필수입니다.")
        String address1,

        @Schema(description = "상세 주소", example = "상세 주소")
        @NotBlank(message = "상세 주소는 필수입니다.")
        String address2,

        @Schema(description = "판매 목록")
        @NotEmpty(message = "최소 하나 이상의 항목은 필수입니다.")
        @Valid
        List<SaleLine> saleLines
) {
    public record SaleLine(
            @Schema(description = "상품 식별자 ID", example = "1")
            @NotNull(message = "상품 식별자 ID는 필수입니다.")
            Long itemId,

            @Schema(description = "창고 식별자 ID", example = "1")
            @NotNull(message = "창고 식별자 ID는 필수입니다.")
            Long warehouseId,

            @Schema(description = "판매 가격", example = "15000.00")
            @NotNull(message = "판매 가격은 필수입니다.")
            @DecimalMin(value = "0.0", inclusive = false)
            @Digits(integer = 13, fraction = 2)
            BigDecimal unitPrice,

            @Schema(description = "수량", example = "100")
            @NotNull(message = "수량은 필수입니다.")
            @Min(value = 1, message = "최소 1 이상입니다.")
            Long qty
    ) {
    }
}