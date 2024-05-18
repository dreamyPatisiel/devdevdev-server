package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.policy.TechArticlePopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.BookmarkRepository;
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
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
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
        // 기술블로그 조회
        SearchHits<ElasticTechArticle> searchHits = elasticTechArticleService.getTechArticles(pageable, elasticId,
                techArticleSort, keyword, companyId, score);

        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 데이터 가공
        List<TechArticleMainResponse> techArticlesResponse = getTechArticlesResponse(searchHits, member);

        return createElasticSlice(pageable, searchHits, techArticlesResponse);
    }

    @Override
    @Transactional
    public TechArticleDetailResponse getTechArticle(Long id, Authentication authentication) {
        // 기술블로그 조회
        TechArticle techArticle = findTechArticle(id);
        ElasticTechArticle elasticTechArticle = findElasticTechArticle(techArticle);
        CompanyResponse companyResponse = CompanyResponse.from(techArticle.getCompany());

        // 조회수 증가
        techArticle.incrementViewCount();
        techArticle.changePopularScore(techArticlePopularScorePolicy);

        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 데이터 가공
        return TechArticleDetailResponse.of(elasticTechArticle, techArticle, companyResponse,
                isBookmarkedByMember(techArticle, member));
    }

    @Override
    @Transactional
    public BookmarkResponse updateBookmark(Long id, boolean status, Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 회원의 해당 개시글 북마크 조회
        TechArticle techArticle = findTechArticle(id);
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

    @Override
    public Slice<TechArticleMainResponse> getBookmarkedTechArticles(Pageable pageable, Long techArticleId,
                                                                    BookmarkSort bookmarkSort,
                                                                    Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 북마크 기술블로그 조회(rds, elasticsearch)
        Slice<TechArticle> techArticleSlices = findBookmarkedTechArticles(pageable, techArticleId, bookmarkSort,
                member);
        List<TechArticle> techArticles = techArticleSlices.getContent();

        // 데이터 가공
        List<TechArticleMainResponse> techArticleMainRespons = mapToTechArticlesResponse(techArticles);

        return new SliceImpl<>(techArticleMainRespons, pageable, techArticleSlices.hasNext());
    }

    private List<TechArticleMainResponse> getTechArticlesResponse(SearchHits<ElasticTechArticle> searchHits,
                                                                  Member member) {
        List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse = mapToElasticTechArticlesResponse(
                searchHits);
        return mapToTechArticlesResponse(elasticTechArticlesResponse, member);
    }

    private List<TechArticleMainResponse> mapToTechArticlesResponse(
            List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse, Member member) {

        // 조회 결과가 없을 경우 빈 리스트 응답
        if (elasticTechArticlesResponse.isEmpty()) {
            return Collections.emptyList();
        }

        List<TechArticle> findTechArticles = getTechArticlesByElasticIdsIn(elasticTechArticlesResponse);
        Map<String, ElasticResponse<ElasticTechArticle>> elasticsResponseMap = getElasticResponseMap(
                elasticTechArticlesResponse);

        return findTechArticles.stream()
                .map(techArticle -> {
                    ElasticResponse<ElasticTechArticle> elasticResponse = elasticsResponseMap.get(
                            techArticle.getElasticId());
                    CompanyResponse companyResponse = createCompanyResponse(techArticle);
                    return TechArticleMainResponse.of(elasticResponse.content(), techArticle, companyResponse,
                            elasticResponse.score(), isBookmarkedByMember(techArticle, member));
                })
                .toList();
    }

    private List<TechArticleMainResponse> mapToTechArticlesResponse(List<TechArticle> techArticles) {
        List<ElasticTechArticle> elasticTechArticles = findElasticTechArticlesByElasticIdsIn(techArticles);
        List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse = mapToElasticTechArticlesResponse(
                elasticTechArticles);
        Map<String, ElasticResponse<ElasticTechArticle>> elasticsResponseMap = getElasticResponseMap(
                elasticTechArticlesResponse);

        return techArticles.stream()
                .map(techArticle -> {
                    ElasticResponse<ElasticTechArticle> elasticResponse = elasticsResponseMap.get(
                            techArticle.getElasticId());
                    CompanyResponse companyResponse = createCompanyResponse(techArticle);
                    return TechArticleMainResponse.of(elasticResponse.content(), techArticle, companyResponse,
                            elasticResponse.score(), true);
                })
                .toList();
    }

    private boolean isBookmarkedByMember(TechArticle techArticle, Member member) {
        boolean isBookmarked = bookmarkRepository.findByTechArticleAndMember(techArticle, member)
                .map(Bookmark::isBookmarked).orElse(false);
        return isBookmarked;
    }
}