package com.erp.mini.purchase.repo;

import com.erp.mini.purchase.dto.PurchaseHeaderDto;
import com.erp.mini.purchase.dto.PurchaseLineDto;
import com.erp.mini.user.domain.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.erp.mini.item.domain.QItem.item;
import static com.erp.mini.partner.domain.QPartner.partner;
import static com.erp.mini.purchase.domain.QPurchaseOrder.purchaseOrder;
import static com.erp.mini.purchase.domain.QPurchaseOrderLine.purchaseOrderLine;
import static com.erp.mini.warehouse.domain.QWarehouse.warehouse;

@RequiredArgsConstructor
public class PurchaseOrderRepositoryImpl implements PurchaseOrderRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public PurchaseHeaderDto getPurchaseDetailHeader(Long purchaseOrderId) {
        QUser createdBy = new QUser("createdBy");
        QUser updatedBy = new QUser("updatedBy");

        return jpaQueryFactory
                .select(Projections.constructor(
                        PurchaseHeaderDto.class,
                        purchaseOrder.id,
                        partner.code,
                        partner.name,
                        purchaseOrder.status,
                        purchaseOrder.createdAt,
                        purchaseOrder.updatedAt,
                        createdBy.name,
                        updatedBy.name
                ))
                .from(purchaseOrder)
                .join(purchaseOrder.partner, partner)
                .leftJoin(createdBy).on(createdBy.id.eq(purchaseOrder.createdBy))
                .leftJoin(updatedBy).on(updatedBy.id.eq(purchaseOrder.updatedBy))
                .where(purchaseOrder.id.eq(purchaseOrderId))
                .fetchOne();
    }

    @Override
    public List<PurchaseLineDto> getPurchaseDetailLines(Long purchaseOrderId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        PurchaseLineDto.class,
                        purchaseOrderLine.id,
                        item.code,
                        item.name,
                        warehouse.code,
                        warehouse.name,
                        warehouse.location,
                        purchaseOrderLine.qty,
                        purchaseOrderLine.unitCost,
                        purchaseOrderLine.unitCost.multiply(purchaseOrderLine.qty)
                ))
                .from(purchaseOrderLine)
                .join(purchaseOrderLine.warehouse, warehouse)
                .join(purchaseOrderLine.item, item)
                .where(purchaseOrderLine.purchaseOrder.id.eq(purchaseOrderId))
                .fetch();
    }
}
