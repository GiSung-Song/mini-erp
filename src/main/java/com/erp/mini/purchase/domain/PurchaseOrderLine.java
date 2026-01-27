package com.erp.mini.purchase.domain;

import com.erp.mini.item.domain.Item;
import com.erp.mini.warehouse.domain.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(
        name = "purchase_order_lines",
        uniqueConstraints = {@UniqueConstraint(name = "uq_purchase_order_lines_purchase_order_item_warehouse",
                columnNames = {"purchase_order_id", "item_id", "warehouse_id"})},
        indexes = @Index(name = "idx_purchase_order_lines_item", columnList = "item_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "purchase_order_id",
            foreignKey = @ForeignKey(name = "fk_purchase_order_lines_purchase_order"))
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "item_id",
            foreignKey = @ForeignKey(name = "fk_purchase_order_lines_item"))
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "warehouse_id",
            foreignKey = @ForeignKey(name = "fk_purchase_order_lines_warehouse"))
    private Warehouse warehouse;

    @Column(nullable = false)
    private long qty;

    @Column(nullable = false, name = "unit_cost")
    private BigDecimal unitCost;

    private PurchaseOrderLine(PurchaseOrder purchaseOrder, Item item, Warehouse warehouse, long qty, BigDecimal unitCost) {
        this.purchaseOrder = purchaseOrder;
        this.item = item;
        this.warehouse = warehouse;
        this.qty = qty;
        this.unitCost = unitCost;
    }

    // static인 이유는 PurchaseOrder로만 생성 가능하게 하기 위함(외부에서 생성 불가능)
    static PurchaseOrderLine createPurchaseOrderLine(
            PurchaseOrder purchaseOrder,
            Item item,
            Warehouse warehouse,
            long qty,
            BigDecimal unitCost
    ) {
        return new PurchaseOrderLine(purchaseOrder, item, warehouse, qty, unitCost);
    }

    boolean sameKey(Item item, Warehouse warehouse) {
        // id 기반으로 같은 객체인지 비교
        return Objects.equals(this.item.getId(), item.getId())
                &&  Objects.equals(this.warehouse.getId(), warehouse.getId());
    }
}
