package com.erp.mini.sales.domain;

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
        name = "sales_order_lines",
        uniqueConstraints = {@UniqueConstraint(name = "uq_sales_ordr_lines_sales_order_item_warehouse",
                columnNames = {"sales_order_id", "item_id", "warehouse_id"})},
        indexes = @Index(name = "idx_sales_order_lines_item", columnList = "item_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "sales_order_id",
            foreignKey = @ForeignKey(name = "fk_sales_order_lines_sales_order"))
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "item_id",
            foreignKey = @ForeignKey(name = "fk_sales_order_lines_item"))
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "warehouse_id",
            foreignKey = @ForeignKey(name = "fk_sales_order_lines_warehouse"))
    private Warehouse warehouse;

    @Column(nullable = false)
    private long qty;

    @Column(nullable = false, name = "unit_price")
    private BigDecimal unitPrice;

    private SalesOrderLine(SalesOrder salesOrder, Item item, Warehouse warehouse, long qty, BigDecimal unitPrice) {
        this.salesOrder = salesOrder;
        this.item = item;
        this.warehouse = warehouse;
        this.qty = qty;
        this.unitPrice = unitPrice;
    }

    // static인 이유는 SalesOrder로만 생성 가능하게 하기 위함(외부에서 생성 불가능)
    static SalesOrderLine createSalesOrderLine(
            SalesOrder salesOrder,
            Item item,
            Warehouse warehouse,
            long qty,
            BigDecimal unitPrice
    ) {
        return new SalesOrderLine(salesOrder, item, warehouse, qty, unitPrice);
    }

    boolean sameKey(Item item, Warehouse warehouse) {
        // id 기반으로 같은 객체인지 비교
        return Objects.equals(this.item.getId(), item.getId())
                &&  Objects.equals(this.warehouse.getId(), warehouse.getId());
    }
}