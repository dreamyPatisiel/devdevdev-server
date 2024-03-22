package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dreamypatisiel.devdevdev.domain.entity.QTechArticle.techArticle;

@RequiredArgsConstructor
public class TechArticleRepositoryImpl implements TechArticleRepositoryCustom {

    private final JPQLQueryFactory query;

    @Override
    public List<TechArticle> findAllByElasticIdIn(List<String> elasticIds) {

        List<TechArticle> findTechArticles = query.selectFrom(techArticle)
                .where(techArticle.elasticId.in(elasticIds))
                .fetch();

        // elasticId 목록의 순서를 기반으로 결과 목록 재정렬(h2 database에서는 order by Field() 쿼리를 지원하지 않으므로 재정렬 필요)
        Map<String, TechArticle> techArticles = findTechArticles.stream()
                .collect(Collectors.toMap(TechArticle::getElasticId, Function.identity()));

        return elasticIds.stream()
                .map(techArticles::get)
                .collect(Collectors.toList());
    }
}
