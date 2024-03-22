package com.dreamypatisiel.devdevdev.elastic.domain.service;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.exception.ElasticTechArticleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.dreamypatisiel.devdevdev.elastic.constant.ElasticsearchConstant._ID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ElasticTechArticleService {

    public static final String NOT_FOUND_ELASTIC_TECH_ARTICLE_EXCEPTION_MESSAGE = "존재하지 않는 엘라스틱 기술블로그 ID 입니다.";

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticTechArticleRepository elasticTechArticleRepository;

    public SearchHits<ElasticTechArticle> findTechArticles(Pageable pageable, String elasticId,
                                                           TechArticleSort techArticleSort) {
        // 쿼리 생성
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withSearchType(SearchType.QUERY_THEN_FETCH)
                .withPageable(pageable)
                // 정렬 조건 설정
                .withSort(sortCondition(techArticleSort))
                .withSort(sortPrimaryCondition())
                .build();

        // searchAfter 설정
        setSearchAfterCondition(elasticId, techArticleSort, searchQuery);

        return elasticsearchOperations.search(searchQuery, ElasticTechArticle.class);
    }

    public SearchHits<ElasticTechArticle> findTechArticlesByKeyword(Pageable pageable, String elasticId, Float score,
                                                                    TechArticleSort techArticleSort, String keyword) {
        // 무조건 keyword가 있다는 가정하에!!

        // 쿼리 생성
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withSearchType(SearchType.QUERY_THEN_FETCH)
                .withPageable(pageable)
                // 쿼리스트링
                .withQuery(QueryBuilders.queryStringQuery(keyword))
                .withSort(SortBuilders.scoreSort().order(SortOrder.DESC))
                .withSort(sortPrimaryCondition())
                // 정렬 조건 설정
                .withSort(sortCondition(techArticleSort))
                .build();

        // searchAfter 설정
        setSearchAfterCondition(elasticId, score, techArticleSort, searchQuery);

        return elasticsearchOperations.search(searchQuery, ElasticTechArticle.class);
    }

    private FieldSortBuilder sortCondition(TechArticleSort techArticleSort) {
        String sortFieldName = getSortFieldName(techArticleSort);
        return SortBuilders.fieldSort(sortFieldName).order(SortOrder.DESC);
    }

    private FieldSortBuilder sortPrimaryCondition() {
        return SortBuilders.fieldSort(_ID).order(SortOrder.DESC);
    }

    private static String getSortFieldName(TechArticleSort techArticleSort) {
        return Optional.ofNullable(techArticleSort).orElse(TechArticleSort.LATEST).getSortFieldName();
    }

    private void setSearchAfterCondition(String elasticId, TechArticleSort techArticleSort,
                                         NativeSearchQuery searchQuery) {
        if(ObjectUtils.isEmpty(elasticId)) {
            return;
        }

        ElasticTechArticle elasticTechArticle = elasticTechArticleRepository.findById(elasticId)
                .orElseThrow(() -> new ElasticTechArticleException(NOT_FOUND_ELASTIC_TECH_ARTICLE_EXCEPTION_MESSAGE));

        searchQuery.setSearchAfter(getSearchAfter(elasticTechArticle, techArticleSort));
    }

    private void setSearchAfterCondition(String elasticId, Float score, TechArticleSort techArticleSort,
                                         NativeSearchQuery searchQuery) {
        if(ObjectUtils.isEmpty(elasticId)) {
            return;
        }

        Float finalScore = Optional.ofNullable(score).orElse(Float.MAX_VALUE);

        ElasticTechArticle elasticTechArticle = elasticTechArticleRepository.findById(elasticId)
                .orElseThrow(() -> new ElasticTechArticleException(NOT_FOUND_ELASTIC_TECH_ARTICLE_EXCEPTION_MESSAGE));

        searchQuery.setSearchAfter(getSearchAfter(elasticTechArticle, finalScore, techArticleSort));
    }

    private List<Object> getSearchAfter(ElasticTechArticle elasticTechArticle, TechArticleSort techArticleSort) {
        techArticleSort = Optional.ofNullable(techArticleSort).orElse(TechArticleSort.LATEST);
        return List.of(techArticleSort.getCursorCondition(elasticTechArticle), elasticTechArticle.getId());
    }

    private List<Object> getSearchAfter(ElasticTechArticle elasticTechArticle, Float score,
                                        TechArticleSort techArticleSort) {
        techArticleSort = Optional.ofNullable(techArticleSort).orElse(TechArticleSort.LATEST);
        return List.of(techArticleSort.getCursorCondition(elasticTechArticle), elasticTechArticle.getId(), score);
    }
}