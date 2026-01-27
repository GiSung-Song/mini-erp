package com.erp.mini.sales.domain;

import com.erp.mini.common.entity.BaseEntity;
import com.erp.mini.item.domain.Item;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.warehouse.domain.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "sales_orders",
        indexes = @Index(name = "idx_sales_orders_customer_created", columnList = "customer_id, created_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "customer_id",
            foreignKey = @ForeignKey(name = "fk_sales_orders_customer"))
    private Partner partner;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private SalesStatus status;

    @OneToMany(
            mappedBy = "salesOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private final List<SalesOrderLine> salesOrderLines = new ArrayList<>();

    private SalesOrder(Partner partner) {
        this.partner = partner;
        this.status = SalesStatus.CREATED;
    }

    public static SalesOrder createSalesOrder(Partner partner) {
        return new SalesOrder(partner);
    }

    public void addLine(Item item, Warehouse warehouse, long qty, BigDecimal unitPrice) {
        ensureEditable();

        if (containsLine(item, warehouse)) {
            throw new IllegalStateException("이미 존재하는 상품/창고 라인이 존재합니다.");
        }

        SalesOrderLine line = SalesOrderLine.createSalesOrderLine(
                this, item, warehouse, qty, unitPrice
        );

        salesOrderLines.add(line);
    }

    private boolean containsLine(Item item, Warehouse warehouse) {
        return salesOrderLines.stream()
                .anyMatch(l -> l.sameKey(item, warehouse));
    }

    private void ensureEditable() {
        if (status != SalesStatus.CREATED) {
            throw new  IllegalStateException("생성된 발주만 추가 가능합니다.");
        }
    }
}
