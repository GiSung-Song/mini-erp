package com.erp.mini.sales.repo;

import com.erp.mini.sales.dto.SalesHeaderDto;
import com.erp.mini.sales.dto.SalesLineDto;
import com.erp.mini.user.domain.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.erp.mini.item.domain.QItem.item;
import static com.erp.mini.partner.domain.QPartner.partner;
import static com.erp.mini.sales.domain.QSalesOrder.salesOrder;
import static com.erp.mini.sales.domain.QSalesOrderLine.salesOrderLine;
import static com.erp.mini.warehouse.domain.QWarehouse.warehouse;

@RequiredArgsConstructor
public class SalesOrderRepositoryImpl implements SalesOrderRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public SalesHeaderDto getSalesDetailHeader(Long salesOrderId) {
        QUser createdBy = new QUser("createdBy");
        QUser updatedBy = new QUser("updatedBy");

        return jpaQueryFactory
                .select(Projections.constructor(
                        SalesHeaderDto.class,
                        salesOrder.id,
                        partner.code,
                        partner.name,
                        salesOrder.orderCustomerInfo.customerName,
                        salesOrder.orderCustomerInfo.customerPhone,
                        salesOrder.shippingAddress.zipcode,
                        salesOrder.shippingAddress.address1,
                        salesOrder.shippingAddress.address2,
                        salesOrder.status,
                        salesOrder.createdAt,
                        salesOrder.updatedAt,
                        createdBy.name,
                        updatedBy.name
                ))
                .from(salesOrder)
                .join(salesOrder.partner, partner)
                .leftJoin(createdBy).on(createdBy.id.eq(salesOrder.createdBy))
                .leftJoin(updatedBy).on(updatedBy.id.eq(salesOrder.updatedBy))
                .where(salesOrder.id.eq(salesOrderId))
                .fetchOne();
    }

    @Override
    public List<SalesLineDto> getSalesDetailLines(Long salesOrderId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        SalesLineDto.class,
                        salesOrderLine.id,
                        item.code,
                        item.name,
                        warehouse.code,
                        warehouse.name,
                        warehouse.location,
                        salesOrderLine.qty,
                        salesOrderLine.unitPrice,
                        salesOrderLine.unitPrice.multiply(salesOrderLine.qty)
                ))
                .from(salesOrderLine)
                .join(salesOrderLine.warehouse, warehouse)
                .join(salesOrderLine.item, item)
                .where(salesOrderLine.salesOrder.id.eq(salesOrderId))
                .fetch();
    }
}
