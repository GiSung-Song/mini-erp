package com.erp.mini.inventory.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInventoryTransaction is a Querydsl query type for InventoryTransaction
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInventoryTransaction extends EntityPathBase<InventoryTransaction> {

    private static final long serialVersionUID = -988825989L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInventoryTransaction inventoryTransaction = new QInventoryTransaction("inventoryTransaction");

    public final com.erp.mini.common.entity.QCreatedOnlyEntity _super = new com.erp.mini.common.entity.QCreatedOnlyEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.erp.mini.item.domain.QItem item;

    public final NumberPath<Long> qtyDelta = createNumber("qtyDelta", Long.class);

    public final StringPath reason = createString("reason");

    public final NumberPath<Long> refId = createNumber("refId", Long.class);

    public final EnumPath<RefType> refType = createEnum("refType", RefType.class);

    public final EnumPath<TransactionType> type = createEnum("type", TransactionType.class);

    public final com.erp.mini.warehouse.domain.QWarehouse warehouse;

    public QInventoryTransaction(String variable) {
        this(InventoryTransaction.class, forVariable(variable), INITS);
    }

    public QInventoryTransaction(Path<? extends InventoryTransaction> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInventoryTransaction(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInventoryTransaction(PathMetadata metadata, PathInits inits) {
        this(InventoryTransaction.class, metadata, inits);
    }

    public QInventoryTransaction(Class<? extends InventoryTransaction> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.item = inits.isInitialized("item") ? new com.erp.mini.item.domain.QItem(forProperty("item")) : null;
        this.warehouse = inits.isInitialized("warehouse") ? new com.erp.mini.warehouse.domain.QWarehouse(forProperty("warehouse")) : null;
    }

}

