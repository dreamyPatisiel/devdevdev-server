package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import com.dreamypatisiel.devdevdev.elastic.data.domain.ElasticResponse;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticTechArticleService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
public class GuestTechArticleService extends TechArticleServiceUtils implements TechArticleService {

    private final ElasticTechArticleService elasticTechArticleService;

    public GuestTechArticleService(TechArticleRepository techArticleRepository, ElasticTechArticleService elasticTechArticleService) {
        super(techArticleRepository);
        this.elasticTechArticleService = elasticTechArticleService;
    }

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
        return mapToTechArticleResponses(elasticResponses);
    }

    private List<TechArticleResponse> mapToTechArticleResponses(List<ElasticResponse<ElasticTechArticle>> elasticResponses) {
        List<String> elasticIds = getElasticIds(elasticResponses);
        List<TechArticle> findTechArticles = getTechArticlesByElasticIdsIn(elasticIds);
        Map<String, ElasticResponse<ElasticTechArticle>> elasticsResponseMap = getElasticResponseMap(elasticResponses);

        return findTechArticles.stream()
                .map(findTechArticle -> {
                    ElasticResponse<ElasticTechArticle> elasticResponse = elasticsResponseMap.get(findTechArticle.getElasticId());
                    return TechArticleResponse.of(elasticResponse.content(), findTechArticle);
                })
                .toList();
    }
}
