package com.erp.mini.partner.dto;

import com.erp.mini.partner.domain.PartnerType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "거래처 검색")
public record SearchPartnerCondition(
        String keyword,
        PartnerType type
) {
}
