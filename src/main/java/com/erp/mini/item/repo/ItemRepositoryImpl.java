package com.erp.mini.item.repo;

import com.erp.mini.item.dto.ItemDetailDto;
import com.erp.mini.item.dto.SearchItemCondition;
import com.erp.mini.item.dto.SearchItemResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.erp.mini.item.domain.QItem.item;
import static com.erp.mini.stock.domain.QStock.stock;
import static com.erp.mini.warehouse.domain.QWarehouse.warehouse;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<SearchItemResponse> search(SearchItemCondition searchItemCondition, Pageable pageable) {
        List<SearchItemResponse> contents = jpaQueryFactory
                .select(Projections.constructor(
                        SearchItemResponse.class,
                        item.id,
                        item.name,
                        item.code,
                        item.status
                ))
                .from(item)
                .where(
                        nameStarts(searchItemCondition.name()),
                        codeStarts(searchItemCondition.code())
                )
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(item.count())
                .from(item)
                .where(
                        nameStarts(searchItemCondition.name()),
                        codeStarts(searchItemCondition.code())
                )
                .fetchOne();

        return new PageImpl<>(contents, pageable, total == null ? 0 : total);
    }

    @Override
    public List<ItemDetailDto> getItemDetail(Long itemId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        ItemDetailDto.class,
                        item.id,
                        item.name,
                        item.code,
                        item.status,
                        warehouse.id,
                        warehouse.name,
                        stock.qty
                ))
                .from(item)
                .leftJoin(stock).on(stock.item.id.eq(item.id))
                .leftJoin(warehouse).on(stock.warehouse.id.eq(warehouse.id))
                .where(item.id.eq(itemId))
                .fetch();
    }

    private BooleanExpression nameStarts(String name) {
        return hasText(name) ? item.name.startsWith(name) : null;
    }

    private BooleanExpression codeStarts(String code) {
        return hasText(code) ? item.code.startsWith(code) : null;
    }
}
