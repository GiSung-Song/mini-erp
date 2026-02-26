package com.erp.mini.stock.dto;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStockKey is a Querydsl query type for StockKey
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QStockKey extends BeanPath<StockKey> {

    private static final long serialVersionUID = 213446415L;

    public static final QStockKey stockKey = new QStockKey("stockKey");

    public final NumberPath<Long> itemId = createNumber("itemId", Long.class);

    public final NumberPath<Long> warehouseId = createNumber("warehouseId", Long.class);

    public QStockKey(String variable) {
        super(StockKey.class, forVariable(variable));
    }

    public QStockKey(Path<? extends StockKey> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStockKey(PathMetadata metadata) {
        super(StockKey.class, metadata);
    }

}

