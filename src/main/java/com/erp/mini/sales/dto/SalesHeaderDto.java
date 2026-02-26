package com.erp.mini.sales.dto;

import com.erp.mini.sales.domain.SalesStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "판매 상세 헤더")
public record SalesHeaderDto(
        @Schema(description = "판매 식별자 ID")
        Long salesOrderId,

        @Schema(description = "거래처 코드")
        String partnerCode,

        @Schema(description = "거래처명")
        String partnerName,

        @Schema(description = "구매자명")
        String customerName,

        @Schema(description = "구매자 번호")
        String customerPhone,

        @Schema(description = "우편번호")
        String zipcode,

        @Schema(description = "주소")
        String address1,

        @Schema(description = "상세 주소")
        String address2,

        @Schema(description = "판매 상태")
        SalesStatus status,

        @Schema(description = "생성 날짜")
        LocalDateTime createdAt,

        @Schema(description = "마지막 변경 날짜")
        LocalDateTime updatedAt,

        @Schema(description = "생성자")
        String createdBy,

        @Schema(description = "수정자")
        String updatedBy
) {
}
