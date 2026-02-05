package com.erp.mini.partner.repo;

import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.partner.dto.SearchPartnerCondition;
import com.erp.mini.partner.dto.SearchPartnerResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.erp.mini.partner.domain.QPartner.partner;

@RequiredArgsConstructor
public class PartnerRepositoryImpl implements PartnerRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<SearchPartnerResponse> search(SearchPartnerCondition condition, Pageable pageable) {
        List<SearchPartnerResponse> contents = jpaQueryFactory
                .select(Projections.constructor(
                        SearchPartnerResponse.class,
                        partner.id,
                        partner.name,
                        partner.code,
                        partner.type
                ))
                .from(partner)
                .where(
                        typeEq(condition.type()),
                        keywordPrefix(condition.keyword())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(partner.name.asc())
                .fetch();

        Long total = jpaQueryFactory
                .select(partner.count())
                .from(partner)
                .where(
                        typeEq(condition.type()),
                        keywordPrefix(condition.keyword())
                )
                .fetchOne();

        return new PageImpl<>(contents, pageable, total == null ? 0 : total);
    }

    private BooleanExpression typeEq(PartnerType type) {
        return type != null ? partner.type.eq(type) : null;
    }

    private BooleanExpression keywordPrefix(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return partner.code.startsWithIgnoreCase(keyword)
                .or(partner.name.startsWithIgnoreCase(keyword));
    }
}
