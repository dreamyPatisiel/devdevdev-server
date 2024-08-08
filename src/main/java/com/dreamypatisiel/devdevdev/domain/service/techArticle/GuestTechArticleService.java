package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import static com.dreamypatisiel.devdevdev.domain.service.response.util.TechArticleResponseUtils.hasNextPage;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.policy.TechArticlePopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.response.BookmarkResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.CompanyResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleDetailResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleMainResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechCommentRegisterResponse;
import com.dreamypatisiel.devdevdev.elastic.data.domain.ElasticResponse;
import com.dreamypatisiel.devdevdev.elastic.data.domain.ElasticSlice;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticTechArticleService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterTechCommentRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class GuestTechArticleService extends TechArticleCommonService implements TechArticleService {

    public static final String INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE = "비회원은 현재 해당 기능을 이용할 수 없습니다.";

    private final ElasticTechArticleService elasticTechArticleService;
    private final TechArticlePopularScorePolicy techArticlePopularScorePolicy;

    public GuestTechArticleService(TechArticleRepository techArticleRepository,
                                   ElasticTechArticleRepository elasticTechArticleRepository,
                                   ElasticTechArticleService elasticTechArticleService,
                                   TechArticlePopularScorePolicy techArticlePopularScorePolicy) {
        super(techArticleRepository, elasticTechArticleRepository);
        this.elasticTechArticleService = elasticTechArticleService;
        this.techArticlePopularScorePolicy = techArticlePopularScorePolicy;
    }

    @Override
    public Slice<TechArticleMainResponse> getTechArticles(Pageable pageable, String elasticId,
                                                          TechArticleSort techArticleSort, String keyword,
                                                          Long companyId, Float score, Authentication authentication) {
        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 엘라스틱서치 기술블로그 조회
        SearchHits<ElasticTechArticle> elasticTechArticleSearchHits = elasticTechArticleService.getTechArticles(
                pageable, elasticId, techArticleSort, keyword, companyId, score);
        List<ElasticResponse<ElasticTechArticle>> elasticTechArticles = elasticTechArticleService.mapToElasticResponse(
                elasticTechArticleSearchHits);

        // 데이터 가공
        List<TechArticleMainResponse> techArticlesResponse = getTechArticlesResponse(elasticTechArticles);

        return new ElasticSlice<>(techArticlesResponse, pageable, elasticTechArticleSearchHits.getTotalHits(),
                hasNextPage(techArticlesResponse, pageable));
    }

    @Override
    @Transactional
    public TechArticleDetailResponse getTechArticle(Long techArticleId, Authentication authentication) {
        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 기술블로그 조회
        TechArticle techArticle = findTechArticle(techArticleId);
        ElasticTechArticle elasticTechArticle = findElasticTechArticle(techArticle);
        CompanyResponse companyResponse = CompanyResponse.from(techArticle.getCompany());

        // 조회수 증가
        techArticle.incrementViewCount();
        techArticle.changePopularScore(techArticlePopularScorePolicy);

        // 데이터 가공
        return TechArticleDetailResponse.of(techArticle, elasticTechArticle, companyResponse);
    }

    @Override
    public BookmarkResponse updateBookmark(Long techArticleId, boolean status, Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public TechCommentRegisterResponse registerTechComment(Long techArticleId,
                                                           RegisterTechCommentRequest registerTechCommentRequest,
                                                           Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    /**
     * 엘라스틱서치 검색 결과로 기술블로그 목록 응답을 생성합니다.
     */
    private List<TechArticleMainResponse> getTechArticlesResponse(
            List<ElasticResponse<ElasticTechArticle>> elasticTechArticles) {
        // 조회 결과가 없을 경우 빈 리스트 응답
        if (elasticTechArticles.isEmpty()) {
            return Collections.emptyList();
        }

        List<TechArticle> techArticles = findTechArticlesByElasticTechArticles(elasticTechArticles);

        return techArticles.stream()
                .flatMap(techArticle -> mapToTechArticlesResponse(techArticle, elasticTechArticles))
                .toList();
    }

    /**
     * 기술블로그 목록을 응답 형태로 가공합니다.
     */
    private Stream<TechArticleMainResponse> mapToTechArticlesResponse(TechArticle techArticle,
                                                                      List<ElasticResponse<ElasticTechArticle>> elasticTechArticles) {
        return elasticTechArticles.stream()
                .filter(elasticTechArticle -> techArticle.getElasticId().equals(elasticTechArticle.content().getId()))
                .map(elasticTechArticle -> TechArticleMainResponse.of(techArticle, elasticTechArticle.content(),
                        CompanyResponse.from(techArticle.getCompany()), elasticTechArticle.score()));
    }
}
