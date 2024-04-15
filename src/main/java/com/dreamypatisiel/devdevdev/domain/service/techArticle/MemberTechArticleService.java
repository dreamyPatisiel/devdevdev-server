package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.response.BookmarkResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.CompanyResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import com.dreamypatisiel.devdevdev.elastic.data.domain.ElasticResponse;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticTechArticleService;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class MemberTechArticleService extends TechArticleCommonService implements TechArticleService {

    private final ElasticTechArticleService elasticTechArticleService;
    private final BookmarkRepository bookmarkRepository;
    private final MemberProvider memberProvider;


    public MemberTechArticleService(TechArticleRepository techArticleRepository,
                                    ElasticTechArticleRepository elasticTechArticleRepository,
                                    ElasticTechArticleService elasticTechArticleService, BookmarkRepository bookmarkRepository,
                                    MemberProvider memberProvider) {
        super(techArticleRepository, elasticTechArticleRepository);
        this.elasticTechArticleService = elasticTechArticleService;
        this.bookmarkRepository = bookmarkRepository;
        this.memberProvider = memberProvider;
    }

    @Override
    public Slice<TechArticleResponse> getTechArticles(Pageable pageable, String elasticId,
                                                         TechArticleSort techArticleSort, String keyword,
                                                         Float score, Authentication authentication) {
        // 기술블로그 조회
        SearchHits<ElasticTechArticle> searchHits = elasticTechArticleService.getTechArticles(pageable, elasticId, techArticleSort, keyword, score);

        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 데이터 가공
        List<TechArticleResponse> techArticlesResponse = getTechArticlesResponse(searchHits, member);

        return createElasticSlice(pageable, searchHits, techArticlesResponse);
    }

    @Override
    public TechArticleResponse getTechArticle(Long id, Authentication authentication) {
        // 기술블로그 조회
        TechArticle techArticle = findTechArticle(id);
        ElasticTechArticle elasticTechArticle = findElasticTechArticle(techArticle);
        CompanyResponse companyResponse = CompanyResponse.from(techArticle.getCompany());

        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 데이터 가공
        return getTechArticleResponse(techArticle, elasticTechArticle, companyResponse, member);
    }

    @Override
    @Transactional
    public BookmarkResponse toggleBookmark(Long id, Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 회원의 해당 개시글 북마크 조회
        TechArticle techArticle = findTechArticle(id);
        Optional<Bookmark> findBookmark = bookmarkRepository.findByTechArticleAndMember(techArticle, member);

        // 북마크 존재하면 toggle, 없으면 true로 생성
        boolean[] value = { true };
        findBookmark.ifPresentOrElse(
                bookmark -> {
                    bookmark.toggleBookmark();
                    bookmarkRepository.save(bookmark);
                    value[0] = bookmark.isBookmarked();
                },
                () -> {
                    Bookmark bookmark = Bookmark.from(member, techArticle, true);
                    bookmarkRepository.save(bookmark);
                }
        );

        return new BookmarkResponse(techArticle.getId(), value[0]);
    }

    private List<TechArticleResponse> getTechArticlesResponse(SearchHits<ElasticTechArticle> searchHits, Member member) {
        List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse = mapToElasticTechArticlesResponse(searchHits);
        return mapToTechArticlesResponse(elasticTechArticlesResponse, member);
    }

    private List<TechArticleResponse> mapToTechArticlesResponse(List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse, Member member) {
        List<TechArticle> findTechArticles = getTechArticlesByElasticIdsIn(elasticTechArticlesResponse);
        Map<String, ElasticResponse<ElasticTechArticle>> elasticsResponseMap = getElasticResponseMap(elasticTechArticlesResponse);

        return findTechArticles.stream()
                .map(findTechArticle -> {
                    ElasticResponse<ElasticTechArticle> elasticResponse = elasticsResponseMap.get(findTechArticle.getElasticId());
                    CompanyResponse companyResponse = createCompanyResponse(findTechArticle);
                    return TechArticleResponse.of(elasticResponse.content(), findTechArticle, companyResponse, elasticResponse.score(), isBookmarkedByMember(findTechArticle, member));
                })
                .toList();
    }

    private TechArticleResponse getTechArticleResponse(TechArticle techArticle, ElasticTechArticle elasticTechArticle, CompanyResponse companyResponse, Member member) {
        return TechArticleResponse.of(elasticTechArticle, techArticle, companyResponse, isBookmarkedByMember(techArticle, member));
    }

    private boolean isBookmarkedByMember(TechArticle techArticle, Member member) {
        Optional<Bookmark> bookmark = bookmarkRepository.findByTechArticleAndMember(techArticle, member);
        return bookmark.isPresent() && bookmark.get().isBookmarked();
    }
}