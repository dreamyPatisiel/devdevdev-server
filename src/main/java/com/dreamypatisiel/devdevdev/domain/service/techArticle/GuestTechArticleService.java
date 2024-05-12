package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.response.BookmarkResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.CompanyResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleDetailResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleMainResponse;
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
import org.springframework.transaction.annotation.Transactional;

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
    public Slice<TechArticleMainResponse> getTechArticles(Pageable pageable, String elasticId,
                                                          TechArticleSort techArticleSort, String keyword,
                                                          Float score, Authentication authentication) {
        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 기술블로그 조회
        SearchHits<ElasticTechArticle> searchHits = elasticTechArticleService.getTechArticles(pageable, elasticId, techArticleSort, keyword, score);

        // 데이터 가공
        List<TechArticleMainResponse> techArticlesResponse = getTechArticlesResponse(searchHits);

        return createElasticSlice(pageable, searchHits, techArticlesResponse);
    }

    @Override
    @Transactional
    public TechArticleDetailResponse getTechArticle(Long id, Authentication authentication) {
        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 기술블로그 조회
        TechArticle techArticle = findTechArticle(id);
        ElasticTechArticle elasticTechArticle = findElasticTechArticle(techArticle);
        CompanyResponse companyResponse = CompanyResponse.from(techArticle.getCompany());

        // 조회수 증가
        techArticle.incrementViewCount();

        // 데이터 가공
        return TechArticleDetailResponse.of(elasticTechArticle, techArticle, companyResponse);
    }

    @Override
    public BookmarkResponse updateBookmark(Long id, boolean status, Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public Slice<TechArticleMainResponse> getBookmarkedTechArticles(Pageable pageable, Long techArticleId, BookmarkSort bookmarkSort, Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    private List<TechArticleMainResponse> getTechArticlesResponse(SearchHits<ElasticTechArticle> searchHits) {
        List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse = mapToElasticTechArticlesResponse(searchHits);
        return mapToTechArticlesResponse(elasticTechArticlesResponse);
    }

    private List<TechArticleMainResponse> mapToTechArticlesResponse(List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse) {
        List<TechArticle> findTechArticles = getTechArticlesByElasticIdsIn(elasticTechArticlesResponse);
        Map<String, ElasticResponse<ElasticTechArticle>> elasticsResponseMap = getElasticResponseMap(elasticTechArticlesResponse);

        return findTechArticles.stream()
                .map(findTechArticle -> {
                    ElasticResponse<ElasticTechArticle> elasticResponse = elasticsResponseMap.get(findTechArticle.getElasticId());
                    CompanyResponse companyResponse = createCompanyResponse(findTechArticle);
                    return TechArticleMainResponse.of(elasticResponse.content(), findTechArticle, companyResponse, elasticResponse.score());
                })
                .toList();
    }
}
