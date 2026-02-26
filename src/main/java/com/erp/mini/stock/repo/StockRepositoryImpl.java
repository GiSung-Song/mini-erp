package com.erp.mini.stock.repo;

import com.erp.mini.stock.domain.Stock;
import com.erp.mini.stock.dto.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.erp.mini.item.domain.QItem.item;
import static com.erp.mini.stock.domain.QStock.stock;
import static com.erp.mini.warehouse.domain.QWarehouse.warehouse;

@RequiredArgsConstructor
public class StockRepositoryImpl implements StockRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public ItemInfoDto getItemInfo(Long itemId) {
        return queryFactory
                .select(Projections.constructor(
                        ItemInfoDto.class,
                        item.id,
                        item.name,
                        item.code,
                        item.status
                ))
                .from(item)
                .where(item.id.eq(itemId))
                .fetchOne();
    }

    @Override
    public Page<ItemStockInfoDto> getItemStockInfo(Long itemId, Pageable pageable) {
        List<ItemStockInfoDto> contents = queryFactory
                .select(Projections.constructor(
                        ItemStockInfoDto.class,
                        warehouse.id,
                        warehouse.name,
                        warehouse.code,
                        warehouse.status,
                        stock.qty
                ))
                .from(stock)
                .join(stock.warehouse, warehouse)
                .where(stock.item.id.eq(itemId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = queryFactory
                .select(stock.count())
                .from(stock)
                .where(stock.item.id.eq(itemId))
                .fetchOne();

        return new PageImpl<>(contents, pageable, count == null ? 0 : count);
    }

    @Override
    public WarehouseInfoDto getWarehouseInfo(Long warehouseId) {
        return queryFactory
                .select(Projections.constructor(
                        WarehouseInfoDto.class,
                        warehouse.id,
                        warehouse.name,
                        warehouse.code,
                        warehouse.location,
                        warehouse.status
                ))
                .from(warehouse)
                .where(warehouse.id.eq(warehouseId))
                .fetchOne();
    }

    @Override
    public Page<WarehouseStockInfoDto> getWarehouseStockInfo(Long warehouseId, Pageable pageable) {
        List<WarehouseStockInfoDto> contents = queryFactory
                .select(Projections.constructor(
                        WarehouseStockInfoDto.class,
                        item.id,
                        item.name,
                        item.code,
                        item.basePrice,
                        item.status,
                        stock.qty
                ))
                .from(stock)
                .join(stock.item, item)
                .where(stock.warehouse.id.eq(warehouseId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = queryFactory
                .select(stock.count())
                .from(stock)
                .where(stock.warehouse.id.eq(warehouseId))
                .fetchOne();

        return new PageImpl<>(contents, pageable, count == null ? 0 : count);
    }

    @Override
    public List<Stock> findAllByKeysForUpdate(List<StockKey> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        BooleanBuilder builder = new BooleanBuilder();

        for (StockKey key : keys) {
            builder.or(
                    stock.item.id.eq(key.getItemId())
                            .and(stock.warehouse.id.eq(key.getWarehouseId()))
            );
        }

        return queryFactory
                .selectFrom(stock)
                .where(builder)
                .orderBy(stock.item.id.asc(), stock.warehouse.id.asc())
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetch();
    }
}
