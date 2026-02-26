package com.erp.mini.inventory.domain;

import com.erp.mini.common.entity.CreatedOnlyEntity;
import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.item.domain.Item;
import com.erp.mini.warehouse.domain.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_transactions", indexes = {
        @Index(name = "idx_inventory_transactions_warehouse_created", columnList = "warehouse_id, created_at"),
        @Index(name = "idx_inventory_transactions_item_created", columnList = "item_id, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryTransaction extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "item_id", foreignKey = @ForeignKey(name = "fk_inventory_transactions_item"))
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "warehouse_id", foreignKey = @ForeignKey(name = "fk_inventory_transactions_warehouse"))
    private Warehouse warehouse;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false, name = "qty_delta")
    private long qtyDelta;

    @Column(length = 20, name = "ref_type")
    @Enumerated(EnumType.STRING)
    private RefType refType;

    @Column(name = "ref_id")
    private Long refId;

    private String reason;

    private InventoryTransaction(Item item, Warehouse warehouse, TransactionType type, long qtyDelta,
            RefType refType, Long refId, String reason) {
        this.item = item;
        this.warehouse = warehouse;
        this.type = type;
        this.qtyDelta = qtyDelta;
        this.refType = refType;
        this.refId = refId;
        this.reason = reason;
    }

    public static InventoryTransaction purchaseInbound(Item item, Warehouse warehouse, long qty, Long refId) {
        if (qty <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "최소 1개 이상이어야합니다.");
        }

        if (refId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "구매 식별자 ID는 필수입니다.");
        }

        return new InventoryTransaction(
                item,
                warehouse,
                TransactionType.INBOUND,
                qty,
                RefType.PURCHASE_ORDER,
                refId,
                null);
    }

    public static InventoryTransaction cancelSalesInbound(Item item, Warehouse warehouse, long qty, Long refId) {
        if (qty <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "최소 1개 이상이어야합니다.");
        }

        if (refId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "취소된 판매 식별자 ID는 필수입니다.");
        }

        return new InventoryTransaction(
                item,
                warehouse,
                TransactionType.INBOUND,
                qty,
                RefType.SALES_ORDER,
                refId,
                null);
    }

    public static InventoryTransaction salesOutbound(Item item, Warehouse warehouse, long qty, Long refId) {
        if (qty <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "최소 1개 이상이어야합니다.");
        }

        if (refId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "판매 식별자 ID는 필수입니다.");
        }

        return new InventoryTransaction(
                item,
                warehouse,
                TransactionType.OUTBOUND,
                -qty,
                RefType.SALES_ORDER,
                refId,
                null);
    }

    public static InventoryTransaction adjust(Item item, Warehouse warehouse, long delta, String reason) {
        if (delta == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "변동 수량이 0일 수 없습니다.");
        }

        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "조정 사유는 필수입니다.");
        }

        return new InventoryTransaction(
                item,
                warehouse,
                TransactionType.ADJUST,
                delta,
                null,
                null,
                reason);
    }
}