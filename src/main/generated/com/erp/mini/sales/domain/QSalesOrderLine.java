package com.erp.mini.sales.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSalesOrderLine is a Querydsl query type for SalesOrderLine
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSalesOrderLine extends EntityPathBase<SalesOrderLine> {

    private static final long serialVersionUID = 1315154495L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSalesOrderLine salesOrderLine = new QSalesOrderLine("salesOrderLine");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.erp.mini.item.domain.QItem item;

    public final NumberPath<Long> qty = createNumber("qty", Long.class);

    public final QSalesOrder salesOrder;

    public final NumberPath<java.math.BigDecimal> unitPrice = createNumber("unitPrice", java.math.BigDecimal.class);

    public final com.erp.mini.warehouse.domain.QWarehouse warehouse;

    public QSalesOrderLine(String variable) {
        this(SalesOrderLine.class, forVariable(variable), INITS);
    }

    public QSalesOrderLine(Path<? extends SalesOrderLine> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSalesOrderLine(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSalesOrderLine(PathMetadata metadata, PathInits inits) {
        this(SalesOrderLine.class, metadata, inits);
    }

    public QSalesOrderLine(Class<? extends SalesOrderLine> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.item = inits.isInitialized("item") ? new com.erp.mini.item.domain.QItem(forProperty("item")) : null;
        this.salesOrder = inits.isInitialized("salesOrder") ? new QSalesOrder(forProperty("salesOrder"), inits.get("salesOrder")) : null;
        this.warehouse = inits.isInitialized("warehouse") ? new com.erp.mini.warehouse.domain.QWarehouse(forProperty("warehouse")) : null;
    }

}

