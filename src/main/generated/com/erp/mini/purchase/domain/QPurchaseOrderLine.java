package com.erp.mini.purchase.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPurchaseOrderLine is a Querydsl query type for PurchaseOrderLine
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPurchaseOrderLine extends EntityPathBase<PurchaseOrderLine> {

    private static final long serialVersionUID = -227924091L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPurchaseOrderLine purchaseOrderLine = new QPurchaseOrderLine("purchaseOrderLine");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.erp.mini.item.domain.QItem item;

    public final QPurchaseOrder purchaseOrder;

    public final NumberPath<Long> qty = createNumber("qty", Long.class);

    public final NumberPath<java.math.BigDecimal> unitCost = createNumber("unitCost", java.math.BigDecimal.class);

    public final com.erp.mini.warehouse.domain.QWarehouse warehouse;

    public QPurchaseOrderLine(String variable) {
        this(PurchaseOrderLine.class, forVariable(variable), INITS);
    }

    public QPurchaseOrderLine(Path<? extends PurchaseOrderLine> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPurchaseOrderLine(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPurchaseOrderLine(PathMetadata metadata, PathInits inits) {
        this(PurchaseOrderLine.class, metadata, inits);
    }

    public QPurchaseOrderLine(Class<? extends PurchaseOrderLine> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.item = inits.isInitialized("item") ? new com.erp.mini.item.domain.QItem(forProperty("item")) : null;
        this.purchaseOrder = inits.isInitialized("purchaseOrder") ? new QPurchaseOrder(forProperty("purchaseOrder"), inits.get("purchaseOrder")) : null;
        this.warehouse = inits.isInitialized("warehouse") ? new com.erp.mini.warehouse.domain.QWarehouse(forProperty("warehouse")) : null;
    }

}

