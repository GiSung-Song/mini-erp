package com.erp.mini.purchase.domain;

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
        name = "purchase_orders",
        indexes = @Index(name = "idx_purchase_orders_supplier_created", columnList = "supplier_id, created_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "supplier_id",
            foreignKey = @ForeignKey(name = "fk_purchase_orders_supplier"))
    private Partner partner;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private PurchaseStatus status;

    // final인 이유는 컬렉션 참조는 불변, 내용만 변경 가능하게 하기 위함(add / remove만 가능)
    // Set이 아닌 List인 이유는 PurchaseOrderLine에서 PurchaseOrder의 id값이 영속화 되기 전이므로 list로 검증
    @OneToMany(
            mappedBy = "purchaseOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private final List<PurchaseOrderLine> purchaseOrderLines = new ArrayList<>();

    private PurchaseOrder(Partner partner) {
        this.partner = partner;
        this.status = PurchaseStatus.CREATED;
    }

    public static PurchaseOrder createPurchaseOrder(Partner partner) {
        return new PurchaseOrder(partner);
    }

    public void addLine(Item item, Warehouse warehouse, long qty, BigDecimal unitCost) {
        ensureCreated();

        if (containsLine(item, warehouse)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 존재하는 상품/창고 라인이 존재합니다.");
        }

        PurchaseOrderLine line = PurchaseOrderLine.createPurchaseOrderLine(
                this, item, warehouse, qty, unitCost
        );

        purchaseOrderLines.add(line);
    }

    public void removeLine(Long lineId) {
        ensureCreated();

        PurchaseOrderLine purchaseOrderLine = purchaseOrderLines.stream()
                .filter(line -> line.getId().equals(lineId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "라인이 존재하지 않습니다."));

        purchaseOrderLines.remove(purchaseOrderLine);
    }

    public void cancel() {
        ensureCancellable();
        this.status = PurchaseStatus.CANCELLED;
    }

    public void markAsOrdered() {
        ensureCreated();

        if (purchaseOrderLines.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "최소 1개 이상의 라인이 필요합니다.");
        }

        this.status = PurchaseStatus.ORDERED;
    }

    public void markAsReceived() {
        ensureOrdered();
        this.status = PurchaseStatus.RECEIVED;
    }

    private boolean containsLine(Item item, Warehouse warehouse) {
        return purchaseOrderLines.stream()
                .anyMatch(l -> l.sameKey(item, warehouse));
    }

    private void ensureCreated() {
        if (status != PurchaseStatus.CREATED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "생성된 발주만 변경 가능합니다.");
        }
    }

    private void ensureCancellable() {
        if (status == PurchaseStatus.RECEIVED || status == PurchaseStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "취소할 수 없는 상태입니다.");
        }
    }

    private void ensureOrdered() {
        if (status != PurchaseStatus.ORDERED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "발주 확정 상태에서만 입고 처리할 수 있습니다.");
        }
    }
}