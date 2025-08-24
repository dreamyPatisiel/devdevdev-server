package com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticleRecommend;
import com.dreamypatisiel.devdevdev.domain.policy.TechArticlePopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.member.AnonymousMemberService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class GuestTechArticleService extends TechArticleCommonService implements TechArticleService {

    public static final String INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE = "비회원은 현재 해당 기능을 이용할 수 없습니다.";

    private final TechArticlePopularScorePolicy techArticlePopularScorePolicy;
    private final AnonymousMemberService anonymousMemberService;
    private final TechArticleRecommendRepository techArticleRecommendRepository;
    private final TechArticleRepository techArticleRepository;

    public GuestTechArticleService(TechArticlePopularScorePolicy techArticlePopularScorePolicy,
                                   AnonymousMemberService anonymousMemberService,
                                   TechArticleRepository techArticleRepository,
                                   TechArticleRecommendRepository techArticleRecommendRepository
   ) {
        super(techArticleRepository);
        this.techArticlePopularScorePolicy = techArticlePopularScorePolicy;
        this.anonymousMemberService = anonymousMemberService;
        this.techArticleRecommendRepository = techArticleRecommendRepository;
        this.techArticleRepository = techArticleRepository;
    }

    @Override
    public Slice<TechArticleMainResponse> getTechArticles(Pageable pageable, Long techArticleId,
                                                          TechArticleSort techArticleSort, String keyword,
                                                          Long companyId, Float score, Authentication authentication) {
        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 기술블로그 조회
        SliceCustom<TechArticle> techArticles = techArticleRepository.findTechArticlesByCursor(
                pageable, techArticleId, techArticleSort, companyId, keyword, score);

        // 데이터 가공
        List<TechArticleMainResponse> techArticlesResponse = techArticles.stream()
                .map(TechArticleMainResponse::of)
                .toList();

        return new SliceCustom<>(techArticlesResponse, pageable, techArticles.hasNext(), techArticles.getTotalElements());
    }

    @Override
    @Transactional
    public TechArticleDetailResponse getTechArticle(Long techArticleId, String anonymousMemberId, Authentication authentication) {
        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 익명 회원을 조회하거나 생성
        AnonymousMember anonymousMember = getAnonymousMemberOrNull(anonymousMemberId);

        // 기술블로그 조회
        TechArticle techArticle = findTechArticle(techArticleId);

        // 조회수 증가
        techArticle.incrementViewTotalCount();
        techArticle.changePopularScore(techArticlePopularScorePolicy);

        // 데이터 가공
        return TechArticleDetailResponse.of(techArticle, anonymousMember);
    }

    @Override
    public BookmarkResponse updateBookmark(Long techArticleId, Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    @Transactional
    public TechArticleRecommendResponse updateRecommend(Long techArticleId, String anonymousMemberId, Authentication authentication) {
        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 익명 회원을 조회하거나 생성
        AnonymousMember anonymousMember = anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);

        // 익명회원의 해당 기술블로그 아티클 추천 조회
        TechArticle techArticle = findTechArticle(techArticleId);
        Optional<TechArticleRecommend> optionalTechArticleRecommend = techArticleRecommendRepository.findByTechArticleAndAnonymousMember(techArticle, anonymousMember);

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
        TechArticleRecommend techArticleRecommend = TechArticleRecommend.create(anonymousMember, techArticle);
        techArticleRecommendRepository.save(techArticleRecommend);

        // 기술블로그 추천 수 증가 및 점수 변경
        techArticle.incrementRecommendTotalCount();
        techArticle.changePopularScore(techArticlePopularScorePolicy);

        return new TechArticleRecommendResponse(techArticle.getId(), techArticleRecommend.isRecommended(), techArticle.getRecommendTotalCount().getCount());
    }

    /**
     * anonymousMemberId가 있으면 익명 회원을 조회 또는 생성하고, 없으면 null 반환
     */
    public AnonymousMember getAnonymousMemberOrNull(String anonymousMemberId) {
        if(!ObjectUtils.isEmpty(anonymousMemberId)) {
            return anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);
        }
        return null;
    }
}
