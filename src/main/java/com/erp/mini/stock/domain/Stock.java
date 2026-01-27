package com.erp.mini.stock.domain;

import com.erp.mini.common.entity.BaseEntity;
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

}
