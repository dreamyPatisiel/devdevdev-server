package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.CompanyResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import com.dreamypatisiel.devdevdev.elastic.data.domain.ElasticResponse;
import com.dreamypatisiel.devdevdev.elastic.data.domain.ElasticSlice;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.TechArticleException;
import com.google.api.client.util.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.*;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class TechArticleCommonService {

    private final TechArticleRepository techArticleRepository;
    private final ElasticTechArticleRepository elasticTechArticleRepository;

    protected ElasticTechArticle findElasticTechArticle(TechArticle techArticle) {
        String elasticId = techArticle.getElasticId();

        if(elasticId == null) {
            throw new TechArticleException(NOT_FOUND_ELASTIC_ID_MESSAGE);
        }

        return elasticTechArticleRepository.findById(elasticId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE));
    }

    protected TechArticle findTechArticle(Long id) {
        return techArticleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_TECH_ARTICLE_MESSAGE));
    }

    protected static List<ElasticResponse<ElasticTechArticle>> mapToElasticTechArticlesResponse(SearchHits<ElasticTechArticle> searchHits) {
        return searchHits.stream()
                .map(searchHit -> new ElasticResponse<>(searchHit.getContent(), searchHit.getScore()))
                .toList();
    }

    protected static List<ElasticResponse<ElasticTechArticle>> mapToElasticTechArticlesResponse(List<ElasticTechArticle> elasticTechArticles) {
        return elasticTechArticles.stream()
                .map(elasticTechArticle -> new ElasticResponse<>(elasticTechArticle, null))
                .toList();
    }

    protected static List<String> getElasticIds(List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse) {
        return elasticTechArticlesResponse.stream()
                .map(elasticResponse -> elasticResponse.content().getId())
                .toList();
    }

    protected List<TechArticle> getTechArticlesByElasticIdsIn(List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse) {
        List<String> elasticIds = getElasticIds(elasticTechArticlesResponse);
        return techArticleRepository.findAllByElasticIdIn(elasticIds);
    }

    protected static Map<String, ElasticResponse<ElasticTechArticle>> getElasticResponseMap(List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse) {
        return elasticTechArticlesResponse.stream()
                .collect(Collectors.toMap(el -> el.content().getId(), Function.identity()));
    }

    protected ElasticSlice<TechArticleResponse> createElasticSlice(Pageable pageable, SearchHits<ElasticTechArticle> searchHits,
                                                                 List<TechArticleResponse> techArticlesResponse) {
        long totalHits = searchHits.getTotalHits();
        boolean hasNext = hasNextPage(pageable, searchHits);
        return new ElasticSlice<>(techArticlesResponse, pageable, totalHits, hasNext);
    }

    protected static CompanyResponse createCompanyResponse(TechArticle findTechArticle) {
        return Optional.ofNullable(findTechArticle.getCompany())
                .map(CompanyResponse::from)
                .orElse(null);
    }

    private boolean hasNextPage(Pageable pageable, SearchHits<ElasticTechArticle> searchHits) {
        return searchHits.getSearchHits().size() >= pageable.getPageSize();
    }

    protected Slice<TechArticle> findBookmarkedTechArticles(Pageable pageable, Long techArticleId, BookmarkSort bookmarkSort, Member member) {
        return techArticleRepository.findBookmarkedByCursor(pageable, techArticleId, bookmarkSort, member);
    }

    protected List<ElasticTechArticle> findElasticTechArticlesByElasticIdsIn(List<TechArticle> techArticles) {
        List<String> elasticIds = getElasticIdsFromTechArticles(techArticles);
        Iterable<ElasticTechArticle> elasticTechArticles = elasticTechArticleRepository.findAllById(elasticIds);
        return Lists.newArrayList(elasticTechArticles);
    }

    protected static List<String> getElasticIdsFromTechArticles(List<TechArticle> techArticles) {
        return techArticles.stream()
                .map(TechArticle::getElasticId)
                .toList();
    }
}
