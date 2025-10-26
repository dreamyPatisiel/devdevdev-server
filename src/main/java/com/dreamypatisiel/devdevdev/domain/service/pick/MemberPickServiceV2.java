package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.policy.PickBestCommentsPolicy;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
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
public class MemberPickServiceV2 extends PickCommonService implements PickServiceV2 {

    private final MemberProvider memberProvider;

    public MemberPickServiceV2(EmbeddingsService embeddingsService, PickRepository pickRepository,
                               MemberProvider memberProvider,
                               PickCommentRepository pickCommentRepository,
                               PickCommentRecommendRepository pickCommentRecommendRepository,
                               PickPopularScorePolicy pickPopularScorePolicy,
                               PickBestCommentsPolicy pickBestCommentsPolicy, TimeProvider timeProvider) {
        super(embeddingsService, pickBestCommentsPolicy, pickPopularScorePolicy, timeProvider, pickRepository,
                pickCommentRepository,
                pickCommentRecommendRepository);
        this.memberProvider = memberProvider;
    }

    /**
     * 픽픽픽 메인 조회 V2
     */
    @Override
    public Slice<PickMainResponseV2> findPicksMain(Pageable pageable, Long pickId, PickSort pickSort,
                                                 String anonymousMemberId, Authentication authentication) {
        // 픽픽픽 조회
        Slice<Pick> picks = pickRepository.findPicksByCursor(pageable, pickId, pickSort);

        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 데이터 가공
        List<PickMainResponseV2> pickMainResponse = picks.stream()
                .map(pick -> PickMainResponseV2.of(pick, member))
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