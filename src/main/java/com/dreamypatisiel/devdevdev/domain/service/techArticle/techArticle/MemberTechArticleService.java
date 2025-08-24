package com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticleRecommend;
import com.dreamypatisiel.devdevdev.domain.policy.TechArticlePopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class MemberTechArticleService extends TechArticleCommonService implements TechArticleService {

    private final TechArticlePopularScorePolicy techArticlePopularScorePolicy;
    private final MemberProvider memberProvider;
    private final BookmarkRepository bookmarkRepository;
    private final TechArticleRecommendRepository techArticleRecommendRepository;
    private final TechArticleRepository techArticleRepository;

    public MemberTechArticleService(TechArticlePopularScorePolicy techArticlePopularScorePolicy,
                                    MemberProvider memberProvider,
                                    BookmarkRepository bookmarkRepository,
                                    TechArticleRecommendRepository techArticleRecommendRepository,
                                    TechArticleRepository techArticleRepository
    ) {
        super(techArticleRepository);
        this.techArticlePopularScorePolicy = techArticlePopularScorePolicy;
        this.bookmarkRepository = bookmarkRepository;
        this.techArticleRecommendRepository = techArticleRecommendRepository;
        this.memberProvider = memberProvider;
        this.techArticleRepository = techArticleRepository;
    }

    @Override
    public Slice<TechArticleMainResponse> getTechArticles(Pageable pageable, Long techArticleId,
                                                          TechArticleSort techArticleSort, String keyword,
                                                          Long companyId, Float score, Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 기술블로그 조회
        SliceCustom<TechArticle> techArticles = techArticleRepository.findTechArticlesByCursor(
                pageable, techArticleId, techArticleSort, companyId, keyword, score);

        // 데이터 가공
        List<TechArticleMainResponse> techArticlesResponse = techArticles.stream()
                .map(techArticle -> TechArticleMainResponse.of(techArticle, member))
                .toList();

        return new SliceCustom<>(techArticlesResponse, pageable, techArticles.hasNext(), techArticles.getTotalElements());
    }

    @Override
    @Transactional
    public TechArticleDetailResponse getTechArticle(Long techArticleId, String anonymousMemberId, Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 기술블로그 조회
        TechArticle techArticle = findTechArticle(techArticleId);

        // 조회수 증가
        techArticle.incrementViewTotalCount();
        techArticle.changePopularScore(techArticlePopularScorePolicy);

        // 데이터 가공
        return TechArticleDetailResponse.of(techArticle, member);
    }

    @Override
    @Transactional
    public BookmarkResponse updateBookmark(Long techArticleId, Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 회원의 해당 기술블로그 아티클 북마크 조회
        TechArticle techArticle = findTechArticle(techArticleId);
        Optional<Bookmark> optionalBookmark = bookmarkRepository.findByTechArticleAndMember(techArticle, member);

        // 북마크가 존재하면 toggle
        if (optionalBookmark.isPresent()) {
            Bookmark bookmark = optionalBookmark.get();

            // 북마크 상태라면 북마크 취소
            if (bookmark.isBookmarked()) {
                bookmark.cancelBookmark();
                return new BookmarkResponse(techArticle.getId(), bookmark.isBookmarked());
            }

            // 북마크 상태가 아니라면 북마크
            bookmark.registerBookmark();
            return new BookmarkResponse(techArticle.getId(), bookmark.isBookmarked());
        }

        // 북마크 생성
        Bookmark bookmark = Bookmark.create(member, techArticle);
        bookmarkRepository.save(bookmark);

        return new BookmarkResponse(techArticle.getId(), bookmark.isBookmarked());
    }

    @Override
    @Transactional
    public TechArticleRecommendResponse updateRecommend(Long techArticleId, String anonymousMemberId, Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 회원의 해당 기술블로그 아티클 추천 조회
        TechArticle techArticle = findTechArticle(techArticleId);

        Optional<TechArticleRecommend> optionalTechArticleRecommend = techArticleRecommendRepository.findByTechArticleAndMember(techArticle, member);

        // 추천이 존재하면 toggle
        if (optionalTechArticleRecommend.isPresent()) {
            TechArticleRecommend techArticleRecommend = optionalTechArticleRecommend.get();

            // 추천 상태라면 추천 취소
            if (techArticleRecommend.isRecommended()) {
                techArticleRecommend.cancelRecommend();

                // 기술블로그 추천 수 감소 및 점수 변경
                techArticle.decrementRecommendTotalCount();
                techArticle.changePopularScore(techArticlePopularScorePolicy);

                return new TechArticleRecommendResponse(techArticle.getId(), techArticleRecommend.isRecommended(), techArticle.getRecommendTotalCount().getCount());
            }

            // 추천 상태가 아니라면 추천
            techArticleRecommend.registerRecommend();

            // 기술블로그 추천 수 증가 및 점수 변경
            techArticle.incrementRecommendTotalCount();
            techArticle.changePopularScore(techArticlePopularScorePolicy);

            return new TechArticleRecommendResponse(techArticle.getId(), techArticleRecommend.isRecommended(), techArticle.getRecommendTotalCount().getCount());
        }

        // 추천 생성
        TechArticleRecommend techArticleRecommend = TechArticleRecommend.create(member, techArticle);

        // 기술블로그 추천 수 증가 및 점수 변경
        techArticle.incrementRecommendTotalCount();
        techArticle.changePopularScore(techArticlePopularScorePolicy);

        techArticleRecommendRepository.save(techArticleRecommend);

        return new TechArticleRecommendResponse(techArticle.getId(), techArticleRecommend.isRecommended(), techArticle.getRecommendTotalCount().getCount());
    }
}