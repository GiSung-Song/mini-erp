package com.erp.mini.item.dto;

import com.erp.mini.item.domain.ItemStatus;

public record ItemDetailDto(
        Long itemId,
        String itemName,
        String itemCode,
        ItemStatus status,
        Long warehouseId,
        String warehouseName,
        Integer quantity
) {
}