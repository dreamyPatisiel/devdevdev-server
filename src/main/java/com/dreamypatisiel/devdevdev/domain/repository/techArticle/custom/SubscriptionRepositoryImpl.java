package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import static com.dreamypatisiel.devdevdev.domain.entity.QSubscription.subscription;

import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SubscriptionRepositoryImpl implements SubscriptionRepositoryCustom {

    private final JPQLQueryFactory query;

    // exists 직접 구현, jpa 에서 exists 사용하면 count(1) 로 조회하기 때문
    @Override
    public Boolean existsByMemberIdAndCompanyId(Long memberId, Long companyId) {

        Integer fetchFirst = query.selectOne().from(subscription)
                .where(subscription.member.id.eq(memberId)
                        .and(subscription.company.id.eq(companyId)))
                .fetchFirst();

        return fetchFirst != null;
    }
}
