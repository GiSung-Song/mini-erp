package com.erp.mini.sales.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QShippingAddress is a Querydsl query type for ShippingAddress
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QShippingAddress extends BeanPath<ShippingAddress> {

    private static final long serialVersionUID = -1532602883L;

    public static final QShippingAddress shippingAddress = new QShippingAddress("shippingAddress");

    public final StringPath address1 = createString("address1");

    public final StringPath address2 = createString("address2");

    public final StringPath zipcode = createString("zipcode");

    public QShippingAddress(String variable) {
        super(ShippingAddress.class, forVariable(variable));
    }

    public QShippingAddress(Path<? extends ShippingAddress> path) {
        super(path.getType(), path.getMetadata());
    }

    public QShippingAddress(PathMetadata metadata) {
        super(ShippingAddress.class, metadata);
    }

}

