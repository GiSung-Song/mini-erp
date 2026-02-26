package com.erp.mini.inventory.repo;

import static com.erp.mini.inventory.domain.QInventoryTransaction.inventoryTransaction;
import static com.erp.mini.item.domain.QItem.item;
import static com.erp.mini.warehouse.domain.QWarehouse.warehouse;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.erp.mini.inventory.domain.RefType;
import com.erp.mini.inventory.domain.TransactionType;
import com.erp.mini.inventory.dto.ItxDetailResponse;
import com.erp.mini.inventory.dto.ItxSearchCondition;
import com.erp.mini.inventory.dto.ItxSearchDto;
import com.erp.mini.partner.domain.QPartner;
import com.erp.mini.purchase.domain.QPurchaseOrder;
import com.erp.mini.sales.domain.QSalesOrder;
import com.erp.mini.user.domain.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InventoryTransactionRepositoryImpl implements InventoryTransactionRepositoryCustom {

        private final JPAQueryFactory queryFactory;

        @Override
        public Page<ItxSearchDto> findInventoryTransaction(ItxSearchCondition condition, Pageable pageable) {
                QUser createdBy = new QUser("createdBy");

                List<ItxSearchDto> contents = queryFactory
                                .select(Projections.constructor(
                                                ItxSearchDto.class,
                                                inventoryTransaction.id,
                                                item.code,
                                                item.name,
                                                warehouse.code,
                                                warehouse.name,
                                                inventoryTransaction.type,
                                                inventoryTransaction.qtyDelta,
                                                inventoryTransaction.createdAt,
                                                createdBy.name))
                                .from(inventoryTransaction)
                                .join(inventoryTransaction.item, item)
                                .join(inventoryTransaction.warehouse, warehouse)
                                .leftJoin(createdBy).on(createdBy.id.eq(inventoryTransaction.createdBy))
                                .where(
                                                eqItemId(condition.itemId()),
                                                eqWarehouseId(condition.warehouseId()),
                                                afterStartDate(condition.startDate()),
                                                beforeEndDate(condition.endDate()),
                                                eqInventoryTransactionType(condition.type()))
                                .orderBy(
                                                inventoryTransaction.createdAt.desc(),
                                                inventoryTransaction.id.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                Long count = queryFactory
                                .select(inventoryTransaction.id.count())
                                .from(inventoryTransaction)
                                .where(
                                                eqItemId(condition.itemId()),
                                                eqWarehouseId(condition.warehouseId()),
                                                afterStartDate(condition.startDate()),
                                                beforeEndDate(condition.endDate()),
                                                eqInventoryTransactionType(condition.type()))
                                .fetchOne();

                return new PageImpl<>(contents, pageable, count != null ? count : 0);
        }

        @Override
        public ItxDetailResponse findInventoryTransactionDetail(Long inventoryTransactionId) {
                QUser createdBy = new QUser("createdBy");

                QSalesOrder salesOrder = new QSalesOrder("salesOrder");
                QPurchaseOrder purchaseOrder = new QPurchaseOrder("purchaseOrder");

                QPartner salesPartner = new QPartner("salesPartner");
                QPartner purchasePartner = new QPartner("purchasePartner");

                return queryFactory
                                .select(Projections.constructor(
                                                ItxDetailResponse.class,
                                                inventoryTransaction.id,
                                                item.code,
                                                item.name,
                                                warehouse.code,
                                                warehouse.name,
                                                inventoryTransaction.type,
                                                inventoryTransaction.qtyDelta,
                                                inventoryTransaction.createdAt,
                                                createdBy.name,
                                                inventoryTransaction.refId,
                                                inventoryTransaction.refType,
                                                inventoryTransaction.reason,
                                                Expressions.stringTemplate(
                                                                "coalesce({0}, {1})",
                                                                salesPartner.code,
                                                                purchasePartner.code),
                                                Expressions.stringTemplate(
                                                                "coalesce({0}, {1})",
                                                                salesPartner.name,
                                                                purchasePartner.name)))
                                .from(inventoryTransaction)
                                .join(inventoryTransaction.item, item)
                                .join(inventoryTransaction.warehouse, warehouse)
                                .leftJoin(createdBy).on(createdBy.id.eq(inventoryTransaction.createdBy))
                                .leftJoin(salesOrder).on(inventoryTransaction.refType.eq(RefType.SALES_ORDER)
                                                .and(inventoryTransaction.refId.eq(salesOrder.id)))
                                .leftJoin(purchaseOrder).on(inventoryTransaction.refType.eq(RefType.PURCHASE_ORDER)
                                                .and(inventoryTransaction.refId.eq(purchaseOrder.id)))
                                .leftJoin(salesOrder.partner, salesPartner)
                                .leftJoin(purchaseOrder.partner, purchasePartner)
                                .where(inventoryTransaction.id.eq(inventoryTransactionId))
                                .fetchOne();
        }

        private BooleanExpression eqItemId(Long itemId) {
                return itemId != null
                                ? inventoryTransaction.item.id.eq(itemId)
                                : null;
        }

        private BooleanExpression eqWarehouseId(Long warehouseId) {
                return warehouseId != null
                                ? inventoryTransaction.warehouse.id.eq(warehouseId)
                                : null;
        }

        private BooleanExpression afterStartDate(LocalDate startDate) {
                return startDate != null
                                ? inventoryTransaction.createdAt.goe(startDate.atStartOfDay())
                                : null;
        }

        private BooleanExpression beforeEndDate(LocalDate endDate) {
                return endDate != null
                                ? inventoryTransaction.createdAt.lt(endDate.plusDays(1).atStartOfDay())
                                : null;
        }

        private BooleanExpression eqInventoryTransactionType(TransactionType type) {
                return type != null
                                ? inventoryTransaction.type.eq(type)
                                : null;
        }
}
