package com.erp.mini.item.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QItemCodeSequence is a Querydsl query type for ItemCodeSequence
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QItemCodeSequence extends EntityPathBase<ItemCodeSequence> {

    private static final long serialVersionUID = 735171439L;

    public static final QItemCodeSequence itemCodeSequence = new QItemCodeSequence("itemCodeSequence");

    public final NumberPath<Byte> id = createNumber("id", Byte.class);

    public final NumberPath<Long> nextVal = createNumber("nextVal", Long.class);

    public QItemCodeSequence(String variable) {
        super(ItemCodeSequence.class, forVariable(variable));
    }

    public QItemCodeSequence(Path<? extends ItemCodeSequence> path) {
        super(path.getType(), path.getMetadata());
    }

    public QItemCodeSequence(PathMetadata metadata) {
        super(ItemCodeSequence.class, metadata);
    }

}

