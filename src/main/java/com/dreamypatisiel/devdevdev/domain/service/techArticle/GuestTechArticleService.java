package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.response.BookmarkResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.CompanyResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import com.dreamypatisiel.devdevdev.elastic.data.domain.ElasticResponse;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticTechArticleService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GuestTechArticleService extends TechArticleCommonService implements TechArticleService {

    public static final String INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE = "비회원은 현재 해당 기능을 이용할 수 없습니다.";

    private final ElasticTechArticleService elasticTechArticleService;

    public GuestTechArticleService(TechArticleRepository techArticleRepository,
                                   ElasticTechArticleRepository elasticTechArticleRepository,
                                   ElasticTechArticleService elasticTechArticleService) {
        super(techArticleRepository, elasticTechArticleRepository);
        this.elasticTechArticleService = elasticTechArticleService;
    }

    @Override
    public Slice<TechArticleResponse> getTechArticles(Pageable pageable, String elasticId,
                                                         TechArticleSort techArticleSort, String keyword,
                                                         Float score, Authentication authentication) {
        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 기술블로그 조회
        SearchHits<ElasticTechArticle> searchHits = elasticTechArticleService.getTechArticles(pageable, elasticId, techArticleSort, keyword, score);

        // 데이터 가공
        List<TechArticleResponse> techArticlesResponse = getTechArticlesResponse(searchHits);

        return createElasticSlice(pageable, searchHits, techArticlesResponse);
    }

    @Override
    public TechArticleResponse getTechArticle(Long id, Authentication authentication) {
        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 기술블로그 조회
        TechArticle techArticle = findTechArticle(id);
        ElasticTechArticle elasticTechArticle = findElasticTechArticle(techArticle);
        CompanyResponse companyResponse = CompanyResponse.from(techArticle.getCompany());

        // 데이터 가공
        return getTechArticleResponse(techArticle, elasticTechArticle, companyResponse);
    }

    @Override
    public BookmarkResponse updateBookmark(Long id, Boolean status, Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    private List<TechArticleResponse> getTechArticlesResponse(SearchHits<ElasticTechArticle> searchHits) {
        List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse = mapToElasticTechArticlesResponse(searchHits);
        return mapToTechArticlesResponse(elasticTechArticlesResponse);
    }

    private List<TechArticleResponse> mapToTechArticlesResponse(List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse) {
        List<TechArticle> findTechArticles = getTechArticlesByElasticIdsIn(elasticTechArticlesResponse);
        Map<String, ElasticResponse<ElasticTechArticle>> elasticsResponseMap = getElasticResponseMap(elasticTechArticlesResponse);

        return findTechArticles.stream()
                .map(findTechArticle -> {
                    ElasticResponse<ElasticTechArticle> elasticResponse = elasticsResponseMap.get(findTechArticle.getElasticId());
                    CompanyResponse companyResponse = createCompanyResponse(findTechArticle);
                    return TechArticleResponse.of(elasticResponse.content(), findTechArticle, companyResponse, elasticResponse.score());
                })
                .toList();
    }

    private TechArticleResponse getTechArticleResponse(TechArticle techArticle, ElasticTechArticle elasticTechArticle, CompanyResponse companyResponse) {
        return TechArticleResponse.of(elasticTechArticle, techArticle, companyResponse);
    }
}
