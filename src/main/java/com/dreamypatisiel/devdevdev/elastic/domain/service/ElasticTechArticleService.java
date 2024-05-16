package com.dreamypatisiel.devdevdev.elastic.domain.service;

import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_ELASTIC_METHODS_CALL_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_CURSOR_SCORE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE;
import static com.dreamypatisiel.devdevdev.elastic.constant.ElasticsearchConstant._COMPANY_ID;
import static com.dreamypatisiel.devdevdev.elastic.constant.ElasticsearchConstant._ID;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.exception.ElasticTechArticleException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticTechArticleService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticTechArticleRepository elasticTechArticleRepository;

    public SearchHits<ElasticTechArticle> getTechArticles(Pageable pageable, String elasticId,
                                                          TechArticleSort techArticleSort, String keyword,
                                                          Long companyId, Float score) {
        if (StringUtils.hasText(keyword)) {
            return findTechArticles(pageable, elasticId, techArticleSort, companyId);
        }

        return searchTechArticles(pageable, elasticId, techArticleSort, keyword, companyId, score);
    }

    private SearchHits<ElasticTechArticle> findTechArticles(Pageable pageable, String elasticId,
                                                            TechArticleSort techArticleSort, Long companyId) {
        // 정렬 기준 검증
        techArticleSort = getValidSort(techArticleSort);

        // 쿼리 생성
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withSearchType(SearchType.QUERY_THEN_FETCH)
                .withPageable(pageable)
                // 정렬 조건 설정
                .withSort(getSortCondition(techArticleSort))
                .withSort(getPrimarySortCondition(_ID));

        // 회사 필터 설정
        setFilterWithCompanyId(companyId, queryBuilder);

        NativeSearchQuery searchQuery = queryBuilder.build();

        // searchAfter 설정
        setSearchAfterCondition(elasticId, techArticleSort, searchQuery);

        return elasticsearchOperations.search(searchQuery, ElasticTechArticle.class);
    }

    private SearchHits<ElasticTechArticle> searchTechArticles(Pageable pageable, String elasticId,
                                                              TechArticleSort techArticleSort, String keyword,
                                                              Long companyId, Float score) {

        // 검색어 유무 확인
        if (StringUtils.hasText(keyword)) {
            throw new ElasticTechArticleException(INVALID_ELASTIC_METHODS_CALL_MESSAGE);
        }

        // 정렬 기준 검증
        techArticleSort = getValidSortWhenSearch(techArticleSort);

        // 쿼리 생성
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withSearchType(SearchType.QUERY_THEN_FETCH)
                .withPageable(pageable)
                // 쿼리스트링
                .withQuery(QueryBuilders.queryStringQuery(keyword))
                // 정렬 조건 설정
                .withSort(getSortCondition(techArticleSort))
                .withSort(getPrimarySortCondition(_ID));

        // 회사 필터 설정
        setFilterWithCompanyId(companyId, queryBuilder);

        NativeSearchQuery searchQuery = queryBuilder.build();

        // searchAfter 설정
        setSearchAfterConditionWhenSearch(elasticId, score, techArticleSort, searchQuery);

        return elasticsearchOperations.search(searchQuery, ElasticTechArticle.class);
    }

    private static void setFilterWithCompanyId(Long companyId, NativeSearchQueryBuilder queryBuilder) {
        if (ObjectUtils.isEmpty(companyId)) {
            return;
        }
        queryBuilder.withFilter(QueryBuilders.termQuery(_COMPANY_ID, companyId));
    }

    private FieldSortBuilder getPrimarySortCondition(String fieldName) {
        return SortBuilders.fieldSort(fieldName).order(SortOrder.DESC);
    }

    private SortBuilder<?> getSortCondition(TechArticleSort techArticleSort) {
        return techArticleSort.getSortCondition();
    }

    private static TechArticleSort getValidSort(TechArticleSort techArticleSort) {
        return Optional.ofNullable(techArticleSort)
                .filter(sort -> sort != TechArticleSort.HIGHEST_SCORE)
                .orElse(TechArticleSort.LATEST);
    }

    private static TechArticleSort getValidSortWhenSearch(TechArticleSort techArticleSort) {
        return Optional.ofNullable(techArticleSort).orElse(TechArticleSort.HIGHEST_SCORE);
    }

    private void setSearchAfterCondition(String elasticId, TechArticleSort techArticleSort,
                                         NativeSearchQuery searchQuery) {
        if (StringUtils.hasText(elasticId)) {
            return;
        }

        ElasticTechArticle elasticTechArticle = elasticTechArticleRepository.findById(elasticId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE));

        searchQuery.setSearchAfter(getSearchAfter(elasticTechArticle, techArticleSort));
    }

    private void setSearchAfterConditionWhenSearch(String elasticId, Float score, TechArticleSort techArticleSort,
                                                   NativeSearchQuery searchQuery) {
        if (StringUtils.hasText(elasticId)) {
            return;
        }

        ElasticTechArticle elasticTechArticle = elasticTechArticleRepository.findById(elasticId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE));

        searchQuery.setSearchAfter(getSearchAfterWhenSearch(elasticTechArticle, techArticleSort, score));
    }

    private List<Object> getSearchAfter(ElasticTechArticle elasticTechArticle, TechArticleSort techArticleSort) {
        return List.of(techArticleSort.getSearchAfterCondition(elasticTechArticle), elasticTechArticle.getId());
    }

    private List<Object> getSearchAfterWhenSearch(ElasticTechArticle elasticTechArticle,
                                                  TechArticleSort techArticleSort,
                                                  Float score) {
        // 정확도순 정렬이 아닌 경우
        if (!TechArticleSort.HIGHEST_SCORE.equals(techArticleSort)) {
            return getSearchAfter(elasticTechArticle, techArticleSort);
        }

        if (ObjectUtils.isEmpty(score)) {
            throw new ElasticTechArticleException(NOT_FOUND_CURSOR_SCORE_MESSAGE);
        }

        return List.of(score, elasticTechArticle.getId());
    }
}
