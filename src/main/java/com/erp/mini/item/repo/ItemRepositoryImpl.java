package com.erp.mini.item.repo;

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

    private BooleanExpression nameStarts(String name) {
        return hasText(name) ? item.name.startsWith(name) : null;
    }

    private BooleanExpression codeStarts(String code) {
        return hasText(code) ? item.code.startsWith(code) : null;
    }
}
