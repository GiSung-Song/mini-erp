package com.erp.mini.sales.domain;

import com.erp.mini.common.entity.BaseEntity;
import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
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

    @Embedded
    private OrderCustomerInfo orderCustomerInfo;

    @Embedded
    private ShippingAddress shippingAddress;

    private SalesOrder(Partner partner, OrderCustomerInfo orderCustomerInfo, ShippingAddress shippingAddress) {
        this.partner = partner;
        this.status = SalesStatus.CREATED;
        this.orderCustomerInfo = orderCustomerInfo;
        this.shippingAddress = shippingAddress;
    }

    public static SalesOrder createSalesOrder(Partner partner, OrderCustomerInfo orderCustomerInfo, ShippingAddress shippingAddress) {
        return new SalesOrder(partner, orderCustomerInfo, shippingAddress);
    }

    public void addLine(Item item, Warehouse warehouse, long qty, BigDecimal unitPrice) {
        ensureCreated();

        if (containsLine(item, warehouse)) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미 존재하는 상품/창고 라인이 존재합니다.");
        }

        SalesOrderLine line = SalesOrderLine.createSalesOrderLine(
                this, item, warehouse, qty, unitPrice
        );

        salesOrderLines.add(line);
    }

    public void removeLine(Long lineId) {
        ensureCreated();

        SalesOrderLine salesOrderLine = salesOrderLines.stream()
                .filter(line -> line.getId().equals(lineId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "라인이 존재하지 않습니다."));

        salesOrderLines.remove(salesOrderLine);
    }

    public void cancel() {
        ensureCancellable();
        this.status = SalesStatus.CANCELLED;
    }

    public void markAsOrdered() {
        availableOrder();
        this.status = SalesStatus.ORDERED;
    }

    public void availableOrder() {
        ensureCreated();

        if (salesOrderLines.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "최소 1개 이상의 라인이 필요합니다.");
        }
    }

    public void markAsShipped() {
        ensureOrdered();
        this.status = SalesStatus.SHIPPED;
    }

    public boolean isOrdered() {
        return this.status == SalesStatus.ORDERED;
    }

    private boolean containsLine(Item item, Warehouse warehouse) {
        return salesOrderLines.stream()
                .anyMatch(l -> l.sameKey(item, warehouse));
    }

    private void ensureCreated() {
        if (status != SalesStatus.CREATED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "생성된 발주만 변경 가능합니다.");
        }
    }

    private void ensureCancellable() {
        if (status == SalesStatus.SHIPPED || status == SalesStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "취소할 수 없는 상태입니다.");
        }
    }

    private void ensureOrdered() {
        if (status != SalesStatus.ORDERED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "발주 확정 상태에서만 배송 완료 처리를 할 수 있습니다.");
        }
    }
}