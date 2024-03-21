package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import com.dreamypatisiel.devdevdev.elastic.data.domain.ElasticResponse;
import com.dreamypatisiel.devdevdev.elastic.data.domain.ElasticSlice;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class TechArticleCommonService {

    private final TechArticleRepository techArticleRepository;

    protected static List<ElasticResponse<ElasticTechArticle>> mapToElasticResponses(SearchHits<ElasticTechArticle> searchHits) {
        return searchHits.stream()
                .map(searchHit -> new ElasticResponse<>(searchHit.getContent(), searchHit.getScore()))
                .toList();
    }

    protected static List<String> getElasticIds(List<ElasticResponse<ElasticTechArticle>> elasticResponses) {
        return elasticResponses.stream()
                .map(elasticResponse -> elasticResponse.content().getId())
                .toList();
    }

    protected List<TechArticle> getTechArticlesByElasticIdsIn(List<String> elasticIds) {
        return techArticleRepository.findAllByElasticIdIn(elasticIds);
    }

    protected static Map<String, ElasticResponse<ElasticTechArticle>> getElasticResponseMap(List<ElasticResponse<ElasticTechArticle>> elasticResponses) {
        return elasticResponses.stream()
                .collect(Collectors.toMap(el -> el.content().getId(), Function.identity()));
    }

    protected ElasticSlice<TechArticleResponse> createElasticSlice(Pageable pageable, SearchHits<ElasticTechArticle> searchHits,
                                                                 List<TechArticleResponse> techArticleResponses) {
        long totalHits = searchHits.getTotalHits();
        boolean hasNext = hasNextPage(pageable, searchHits);
        return new ElasticSlice<>(techArticleResponses, pageable, totalHits, hasNext);
    }

    protected boolean hasNextPage(Pageable pageable, SearchHits<ElasticTechArticle> searchHits) {
        return searchHits.getSearchHits().size() >= pageable.getPageSize();
    }
}
