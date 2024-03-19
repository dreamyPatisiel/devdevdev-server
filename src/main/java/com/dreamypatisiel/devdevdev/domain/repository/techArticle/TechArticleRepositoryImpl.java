package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
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

        List<TechArticle> techArticles = query.selectFrom(techArticle)
                .where(techArticle.elasticId.in(elasticIds))
                .fetch();

        // elasticId 목록의 순서를 기반으로 결과 목록 재정렬
        Map<String, TechArticle> techArticleMap = techArticles.stream()
                .collect(Collectors.toMap(TechArticle::getElasticId, Function.identity()));

        return elasticIds.stream()
                .map(techArticleMap::get)
                .collect(Collectors.toList());
    }
}
