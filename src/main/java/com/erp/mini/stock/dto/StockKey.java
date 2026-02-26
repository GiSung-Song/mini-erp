package com.erp.mini.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Schema(description = "재고 엔티티 조회 및 생성을 위한 키 조합")
public class StockKey {
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "warehouse_id")
    private Long warehouseId;
}