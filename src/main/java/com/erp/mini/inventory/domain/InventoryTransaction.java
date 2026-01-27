package com.erp.mini.inventory.domain;

import com.erp.mini.common.entity.CreatedOnlyEntity;
import com.erp.mini.item.domain.Item;
import com.erp.mini.warehouse.domain.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "inventory_transactions",
        indexes = {
                @Index(name = "idx_inventory_transactions_item_warehouse_created", columnList = "item_id, warehouse_id, created_at"),
                @Index(name = "idx_inventory_transactions_item_created", columnList = "item_id, created_at"),
                @Index(name = "idx_inventory_transactions_ref", columnList = "ref_type, ref_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryTransaction extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "item_id",
            foreignKey = @ForeignKey(name = "fk_inventory_transactions_item"))
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "warehouse_id",
            foreignKey = @ForeignKey(name = "fk_inventory_transactions_warehouse"))
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

    @Column(nullable = false)
    private String reason;
}