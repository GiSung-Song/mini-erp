package com.erp.mini.warehouse.repo;

import com.erp.mini.warehouse.domain.WarehouseStatus;
import com.erp.mini.warehouse.dto.SearchWarehouseCondition;
import com.erp.mini.warehouse.dto.SearchWarehouseResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.erp.mini.warehouse.domain.QWarehouse.warehouse;

@RequiredArgsConstructor
public class WarehouseRepositoryImpl implements WarehouseRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<SearchWarehouseResponse> search(SearchWarehouseCondition condition, Pageable pageable) {
        List<SearchWarehouseResponse> contents = jpaQueryFactory
                .select(Projections.constructor(
                        SearchWarehouseResponse.class,
                        warehouse.id,
                        warehouse.name,
                        warehouse.code,
                        warehouse.location,
                        warehouse.status
                ))
                .from(warehouse)
                .where(
                        statusEq(condition.status()),
                        keywordPrefix(condition.keyword())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(warehouse.name.asc())
                .fetch();

        Long total = jpaQueryFactory
                .select(warehouse.count())
                .from(warehouse)
                .where(
                        statusEq(condition.status()),
                        keywordPrefix(condition.keyword())
                )
                .fetchOne();

        return new PageImpl<SearchWarehouseResponse>(contents, pageable, total == null ? 0 : total);
    }

    private BooleanExpression statusEq(WarehouseStatus status) {
        return status != null ? warehouse.status.eq(status) : null;
    }

    private BooleanExpression keywordPrefix(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return warehouse.name.startsWithIgnoreCase(keyword)
                .or(warehouse.location.startsWithIgnoreCase(keyword));
    }
}
