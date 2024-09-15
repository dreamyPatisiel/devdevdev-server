package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import static com.dreamypatisiel.devdevdev.web.dto.util.TechArticleResponseUtils.hasNextPage;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.policy.TechArticlePopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.BookmarkResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.CompanyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import com.dreamypatisiel.devdevdev.elastic.data.response.ElasticResponse;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticTechArticleService;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class MemberTechArticleService extends TechArticleCommonService implements TechArticleService {

    private final ElasticTechArticleService elasticTechArticleService;
    private final TechArticlePopularScorePolicy techArticlePopularScorePolicy;
    private final BookmarkRepository bookmarkRepository;
    private final MemberProvider memberProvider;


    public MemberTechArticleService(TechArticleRepository techArticleRepository,
                                    ElasticTechArticleRepository elasticTechArticleRepository,
                                    ElasticTechArticleService elasticTechArticleService,
                                    TechArticlePopularScorePolicy techArticlePopularScorePolicy,
                                    BookmarkRepository bookmarkRepository,
                                    MemberProvider memberProvider) {
        super(techArticleRepository, elasticTechArticleRepository);
        this.elasticTechArticleService = elasticTechArticleService;
        this.techArticlePopularScorePolicy = techArticlePopularScorePolicy;
        this.bookmarkRepository = bookmarkRepository;
        this.memberProvider = memberProvider;
    }

    @Override
    public Slice<TechArticleMainResponse> getTechArticles(Pageable pageable, String elasticId,
                                                          TechArticleSort techArticleSort, String keyword,
                                                          Long companyId, Float score, Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 엘라스틱서치 기술블로그 조회
        SearchHits<ElasticTechArticle> elasticTechArticleSearchHits = elasticTechArticleService.getTechArticles(
                pageable, elasticId, techArticleSort, keyword, companyId, score);
        List<ElasticResponse<ElasticTechArticle>> elasticTechArticles = elasticTechArticleService.mapToElasticResponse(
                elasticTechArticleSearchHits);

        // 데이터 가공
        List<TechArticleMainResponse> techArticlesResponse = getTechArticlesResponse(elasticTechArticles, member);

        return new SliceCustom<>(techArticlesResponse, pageable, hasNextPage(techArticlesResponse, pageable),
                elasticTechArticleSearchHits.getTotalHits());
    }

    @Override
    @Transactional
    public TechArticleDetailResponse getTechArticle(Long techArticleId, Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 기술블로그 조회
        TechArticle techArticle = findTechArticle(techArticleId);
        ElasticTechArticle elasticTechArticle = findElasticTechArticle(techArticle);
        CompanyResponse companyResponse = CompanyResponse.from(techArticle.getCompany());

        // 조회수 증가
        techArticle.incrementViewTotalCount();
        techArticle.changePopularScore(techArticlePopularScorePolicy);

        // 데이터 가공
        return TechArticleDetailResponse.of(techArticle, elasticTechArticle, companyResponse, member);
    }

    @Override
    @Transactional
    public BookmarkResponse updateBookmark(Long techArticleId, boolean status, Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 회원의 해당 개시글 북마크 조회
        TechArticle techArticle = findTechArticle(techArticleId);
        Optional<Bookmark> findBookmark = bookmarkRepository.findByTechArticleAndMember(techArticle, member);

        // 북마크 존재하면 갱신, 없으면 생성
        findBookmark.ifPresentOrElse(
                bookmark -> bookmark.changeStatus(status),
                () -> {
                    Bookmark bookmark = Bookmark.create(member, techArticle, status);
                    bookmarkRepository.save(bookmark);
                }
        );

        return new BookmarkResponse(techArticle.getId(), status);
    }

    /**
     * 엘라스틱서치 검색 결과로 기술블로그 목록 응답을 생성합니다.
     */
    private List<TechArticleMainResponse> getTechArticlesResponse(
            List<ElasticResponse<ElasticTechArticle>> elasticTechArticles, Member member) {
        // 조회 결과가 없을 경우 빈 리스트 응답
        if (elasticTechArticles.isEmpty()) {
            return Collections.emptyList();
        }

        List<TechArticle> techArticles = findTechArticlesByElasticTechArticles(elasticTechArticles);

        return techArticles.stream()
                .flatMap(techArticle -> mapToTechArticlesResponse(techArticle, elasticTechArticles, member))
                .toList();
    }

    /**
     * 기술블로그 목록을 응답 형태로 가공합니다.
     */
    private Stream<TechArticleMainResponse> mapToTechArticlesResponse(TechArticle techArticle,
                                                                      List<ElasticResponse<ElasticTechArticle>> elasticTechArticles,
                                                                      Member member) {
        return elasticTechArticles.stream()
                .filter(elasticTechArticle -> techArticle.getElasticId().equals(elasticTechArticle.content().getId()))
                .map(elasticTechArticle -> TechArticleMainResponse.of(techArticle, elasticTechArticle.content(),
                        CompanyResponse.from(techArticle.getCompany()), elasticTechArticle.score(), member));
    }
}