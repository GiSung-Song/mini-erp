package com.erp.mini.sales.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrderCustomerInfo is a Querydsl query type for OrderCustomerInfo
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QOrderCustomerInfo extends BeanPath<OrderCustomerInfo> {

    private static final long serialVersionUID = 819537393L;

    public static final QOrderCustomerInfo orderCustomerInfo = new QOrderCustomerInfo("orderCustomerInfo");

    public final StringPath customerName = createString("customerName");

    public final StringPath customerPhone = createString("customerPhone");

    public QOrderCustomerInfo(String variable) {
        super(OrderCustomerInfo.class, forVariable(variable));
    }

    public QOrderCustomerInfo(Path<? extends OrderCustomerInfo> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderCustomerInfo(PathMetadata metadata) {
        super(OrderCustomerInfo.class, metadata);
    }

}

