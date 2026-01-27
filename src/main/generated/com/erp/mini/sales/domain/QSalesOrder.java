package com.erp.mini.sales.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSalesOrder is a Querydsl query type for SalesOrder
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSalesOrder extends EntityPathBase<SalesOrder> {

    private static final long serialVersionUID = -424193109L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSalesOrder salesOrder = new QSalesOrder("salesOrder");

    public final com.erp.mini.common.entity.QBaseEntity _super = new com.erp.mini.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.erp.mini.partner.domain.QPartner partner;

    public final ListPath<SalesOrderLine, QSalesOrderLine> salesOrderLines = this.<SalesOrderLine, QSalesOrderLine>createList("salesOrderLines", SalesOrderLine.class, QSalesOrderLine.class, PathInits.DIRECT2);

    public final EnumPath<SalesStatus> status = createEnum("status", SalesStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public QSalesOrder(String variable) {
        this(SalesOrder.class, forVariable(variable), INITS);
    }

    public QSalesOrder(Path<? extends SalesOrder> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSalesOrder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSalesOrder(PathMetadata metadata, PathInits inits) {
        this(SalesOrder.class, metadata, inits);
    }

    public QSalesOrder(Class<? extends SalesOrder> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.partner = inits.isInitialized("partner") ? new com.erp.mini.partner.domain.QPartner(forProperty("partner")) : null;
    }

}

