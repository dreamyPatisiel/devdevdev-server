package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_ELASTIC_ID_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_TECH_ARTICLE_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.CompanyResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleMainResponse;
import com.dreamypatisiel.devdevdev.elastic.data.domain.ElasticResponse;
import com.dreamypatisiel.devdevdev.elastic.data.domain.ElasticSlice;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.TechArticleException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TechArticleCommonService {

    private final TechArticleRepository techArticleRepository;
    private final ElasticTechArticleRepository elasticTechArticleRepository;

    protected ElasticTechArticle findElasticTechArticle(TechArticle techArticle) {
        String elasticId = techArticle.getElasticId();

        if (!StringUtils.hasText(elasticId)) {
            throw new TechArticleException(NOT_FOUND_ELASTIC_ID_MESSAGE);
        }

        return elasticTechArticleRepository.findById(elasticId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE));
    }

    protected TechArticle findTechArticle(Long id) {
        return techArticleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_TECH_ARTICLE_MESSAGE));
    }

    protected static List<ElasticResponse<ElasticTechArticle>> mapToElasticTechArticlesResponse(
            SearchHits<ElasticTechArticle> searchHits) {
        return searchHits.stream()
                .map(searchHit -> new ElasticResponse<>(searchHit.getContent(), searchHit.getScore()))
                .toList();
    }

    public List<ElasticResponse<ElasticTechArticle>> mapToElasticTechArticlesResponse(
            List<ElasticTechArticle> elasticTechArticles) {
        return elasticTechArticles.stream()
                .map(elasticTechArticle -> new ElasticResponse<>(elasticTechArticle, null))
                .toList();
    }

    public static List<String> getElasticIds(List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse) {
        return elasticTechArticlesResponse.stream()
                .map(elasticResponse -> elasticResponse.content().getId())
                .toList();
    }

    protected List<TechArticle> getTechArticlesByElasticIdsIn(
            List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse) {
        List<String> elasticIds = getElasticIds(elasticTechArticlesResponse);

        // 추출한 elasticId가 없다면 빈 리스트 응답
        if (elasticIds.isEmpty()) {
            return Collections.emptyList();
        }

        return techArticleRepository.findAllByElasticIdIn(elasticIds);
    }

    public static Map<String, ElasticResponse<ElasticTechArticle>> getElasticResponseMap(
            List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse) {
        return elasticTechArticlesResponse.stream()
                .collect(Collectors.toMap(el -> el.content().getId(), Function.identity()));
    }

    protected ElasticSlice<TechArticleMainResponse> createElasticSlice(Pageable pageable,
                                                                       SearchHits<ElasticTechArticle> searchHits,
                                                                       List<TechArticleMainResponse> techArticlesResponse) {
        long totalHits = searchHits.getTotalHits();
        boolean hasNext = hasNextPage(pageable, searchHits);
        return new ElasticSlice<>(techArticlesResponse, pageable, totalHits, hasNext);
    }

    public static CompanyResponse createCompanyResponse(TechArticle findTechArticle) {
        return Optional.ofNullable(findTechArticle.getCompany())
                .map(CompanyResponse::from)
                .orElse(null);
    }

    protected boolean hasNextPage(Pageable pageable, SearchHits<ElasticTechArticle> searchHits) {
        return searchHits.getSearchHits().size() >= pageable.getPageSize();
    }

    public List<ElasticTechArticle> findElasticTechArticlesByElasticIdsIn(List<TechArticle> techArticles) {
        List<String> elasticIds = getElasticIdsFromTechArticles(techArticles);
        Iterable<ElasticTechArticle> elasticTechArticles = elasticTechArticleRepository.findAllById(elasticIds);

        return StreamSupport.stream(elasticTechArticles.spliterator(), false)
                .toList();
    }

    protected static List<String> getElasticIdsFromTechArticles(List<TechArticle> techArticles) {
        return techArticles.stream()
                .map(TechArticle::getElasticId)
                .toList();
    }
}
