package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.policy.PickBestCommentsPolicy;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.openai.embeddings.EmbeddingsService;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.SimilarPickResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MemberPickServiceV2 extends PickCommonService implements PickServiceV2 {

    public MemberPickServiceV2(EmbeddingsService embeddingsService,
                               PickBestCommentsPolicy pickBestCommentsPolicy,
                               PickPopularScorePolicy pickPopularScorePolicy,
                               TimeProvider timeProvider,
                               PickRepository pickRepository,
                               PickCommentRepository pickCommentRepository,
                               PickCommentRecommendRepository pickCommentRecommendRepository) {
        super(embeddingsService, pickBestCommentsPolicy, pickPopularScorePolicy, timeProvider, pickRepository,
                pickCommentRepository, pickCommentRecommendRepository);
    }

    @Override
    public Slice<PickMainResponse> findPicksMain(Pageable pageable, Long pickId, PickSort pickSort, String anonymousMemberId, Authentication authentication) {
        return null;
    }

    @Override
    public PickDetailResponse findPickDetail(Long pickId, String anonymousMemberId, Authentication authentication) {
        return null;
    }

    @Override
    public List<SimilarPickResponse> findTop3SimilarPicks(Long pickId) {
        return null;
    }

}
