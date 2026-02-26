package com.erp.mini.stock.domain;

import com.erp.mini.common.entity.BaseEntity;
import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.item.domain.Item;
import com.erp.mini.warehouse.domain.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "stocks",
        uniqueConstraints = {@UniqueConstraint(name = "uq_stocks_item_warehouse", columnNames = {"item_id", "warehouse_id"})},
        indexes = @Index(name = "idx_stocks_warehouse", columnList = "warehouse_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "item_id",
            foreignKey = @ForeignKey(name = "fk_stocks_item"))
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "warehouse_id",
            foreignKey = @ForeignKey(name = "fk_stocks_warehouse"))
    private Warehouse warehouse;

    @Column(nullable = false)
    private long qty;

    private Stock(Item item, Warehouse warehouse) {
        this.item = item;
        this.warehouse = warehouse;
        this.qty = 0;
    }

    public static Stock createStock(Item item, Warehouse warehouse) {
        return new Stock(item, warehouse);
    }

    public void increase(long qty) {
        if (qty <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "최소 1개 이상이어야 합니다.");
        }

        this.qty += qty;
    }

    public void decrease(long qty) {
        if (qty <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "최소 1개 이상이어야 합니다.");
        }

        if (qty > this.qty) {
            throw new BusinessException(ErrorCode.CONFLICT, "수량이 부족합니다.");
        }

        this.qty -= qty;
    }

    public void adjust(long deltaQty) {
        this.qty += deltaQty;
    }
}
