package com.erp.mini.warehouse.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QWarehouse is a Querydsl query type for Warehouse
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWarehouse extends EntityPathBase<Warehouse> {

    private static final long serialVersionUID = 50516803L;

    public static final QWarehouse warehouse = new QWarehouse("warehouse");

    public final com.erp.mini.common.entity.QBaseEntity _super = new com.erp.mini.common.entity.QBaseEntity(this);

    public final StringPath code = createString("code");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath location = createString("location");

    public final StringPath name = createString("name");

    public final EnumPath<WarehouseStatus> status = createEnum("status", WarehouseStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public QWarehouse(String variable) {
        super(Warehouse.class, forVariable(variable));
    }

    public QWarehouse(Path<? extends Warehouse> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWarehouse(PathMetadata metadata) {
        super(Warehouse.class, metadata);
    }

}

