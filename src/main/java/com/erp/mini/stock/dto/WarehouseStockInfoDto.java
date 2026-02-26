package com.erp.mini.stock.dto;

import com.erp.mini.item.domain.ItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "창고 재고 정보")
public record WarehouseStockInfoDto(
        Long itemId,
        String itemName,
        String itemCode,
        BigDecimal basePrice,
        ItemStatus itemStatus,
        long qty
) {
}
