package com.erp.mini.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 검색 ")
public record SearchItemCondition(
        String code,
        String name
) {
}