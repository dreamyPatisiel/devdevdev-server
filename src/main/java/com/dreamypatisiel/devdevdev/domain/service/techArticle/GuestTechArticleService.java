package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import com.dreamypatisiel.devdevdev.elastic.data.domain.ElasticResponse;
import com.dreamypatisiel.devdevdev.elastic.data.domain.ElasticSlice;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticTechArticleService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestTechArticleService implements TechArticleService {

    private final ElasticTechArticleService elasticTechArticleService;
    private final TechArticleRepository techArticleRepository;

    @Override
    public Slice<TechArticleResponse> findTechArticles(Pageable pageable, String elasticId,
                                                       TechArticleSort techArticleSort, Authentication authentication) {
        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 기술블로그 조회
        SearchHits<ElasticTechArticle> searchHits = elasticTechArticleService.findTechArticles(pageable, elasticId, techArticleSort);

        // 데이터 가공
        List<TechArticleResponse> techArticleResponses = getTechArticleResponses(searchHits);

        return createElasticSlice(pageable, searchHits, techArticleResponses);
    }

    private List<TechArticleResponse> getTechArticleResponses(SearchHits<ElasticTechArticle> searchHits) {
        List<ElasticResponse<ElasticTechArticle>> elasticResponses = mapToElasticResponses(searchHits);
        List<TechArticleResponse> techArticleResponses = mapToTechArticleResponses(elasticResponses);
        return techArticleResponses;
    }

    private static List<ElasticResponse<ElasticTechArticle>> mapToElasticResponses(SearchHits<ElasticTechArticle> searchHits) {
        return searchHits.stream()
                .map(searchHit -> new ElasticResponse<>(searchHit.getContent(), searchHit.getScore()))
                .toList();
    }

    private List<TechArticleResponse> mapToTechArticleResponses(List<ElasticResponse<ElasticTechArticle>> elasticResponses) {
        List<String> elasticIds = elasticResponses.stream()
                .map(elasticResponse -> elasticResponse.content().getId())
                .toList();

        List<TechArticle> findTechArticles = techArticleRepository.findAllByElasticIdIn(elasticIds);

        Map<String, ElasticResponse<ElasticTechArticle>> elasticsResponse = elasticResponses.stream()
                .collect(Collectors.toMap(el -> el.content().getId(), Function.identity()));

        List<TechArticleResponse> techArticleResponses = findTechArticles.stream()
                .map(findTechArticle -> {
                    ElasticResponse<ElasticTechArticle> elasticResponse = elasticsResponse.get(findTechArticle.getElasticId());
                    return TechArticleResponse.of(elasticResponse.content(), findTechArticle);
                })
                .toList();

        return techArticleResponses;
    }

    private ElasticSlice<TechArticleResponse> createElasticSlice(Pageable pageable, SearchHits<ElasticTechArticle> searchHits,
                                                                 List<TechArticleResponse> techArticleResponses) {
        long totalHits = searchHits.getTotalHits();
        boolean hasNext = hasNextPage(pageable, searchHits);
        return new ElasticSlice<>(techArticleResponses, pageable, totalHits, hasNext);
    }

    private boolean hasNextPage(Pageable pageable, SearchHits<ElasticTechArticle> searchHits) {
        return searchHits.getSearchHits().size() >= pageable.getPageSize();
    }

}
