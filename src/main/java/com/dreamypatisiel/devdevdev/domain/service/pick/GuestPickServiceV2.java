package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.policy.PickBestCommentsPolicy;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.pick.*;
import com.dreamypatisiel.devdevdev.domain.service.member.AnonymousMemberService;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.openai.embeddings.EmbeddingsService;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainResponseV2;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.SimilarPickResponseV2;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GuestPickServiceV2 extends PickCommonService implements PickServiceV2 {

    private final AnonymousMemberService anonymousMemberService;

    public GuestPickServiceV2(PickRepository pickRepository, EmbeddingsService embeddingsService,
                              PickBestCommentsPolicy pickBestCommentsPolicy,
                              PickCommentRepository pickCommentRepository,
                              PickCommentRecommendRepository pickCommentRecommendRepository,
                              PickPopularScorePolicy pickPopularScorePolicy,
                              TimeProvider timeProvider, AnonymousMemberService anonymousMemberService) {
        super(embeddingsService, pickBestCommentsPolicy, pickPopularScorePolicy, timeProvider, pickRepository,
                pickCommentRepository, pickCommentRecommendRepository);
        this.anonymousMemberService = anonymousMemberService;
    }

    /**
     * 픽픽픽 메인 조회 V2
     */
    @Transactional
    @Override
    public Slice<PickMainResponseV2> findPicksMain(Pageable pageable, Long pickId, PickSort pickSort,
                                                 String anonymousMemberId, Authentication authentication) {
        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // anonymousMemberId 검증
        AnonymousMember anonymousMember = anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);

        // 픽픽픽 조회
        Slice<Pick> picks = pickRepository.findPicksByCursor(pageable, pickId, pickSort);

        // 데이터 가공
        List<PickMainResponseV2> pickMainResponse = picks.stream()
                .map(pick -> PickMainResponseV2.of(pick, anonymousMember))
                .toList();

        // 전체 픽픽픽 개수 조회
        long totalElements = pickRepository.countByContentStatus(ContentStatus.APPROVAL);

        return new SliceCustom<>(pickMainResponse, pageable, picks.hasNext(), totalElements);
    }

    /**
     * 나도 고민했는데 픽픽픽 V2
     */
    @Override
    public List<SimilarPickResponseV2> findTop3SimilarPicksV2(Long pickId) {
        return super.findTop3SimilarPicksV2(pickId);
    }
}