package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.GuestExceptionMessage.INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.policy.PickBestCommentsPolicy;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.service.pick.dto.PickCommentDto;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.openai.embeddings.EmbeddingsService;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentRecommendResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentsResponse;
import java.util.EnumSet;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GuestPickCommentService extends PickCommonService implements PickCommentService {


    public GuestPickCommentService(EmbeddingsService embeddingsService,
                                   PickBestCommentsPolicy pickBestCommentsPolicy,
                                   TimeProvider timeProvider,
                                   PickRepository pickRepository,
                                   PickPopularScorePolicy pickPopularScorePolicy,
                                   PickCommentRepository pickCommentRepository,
                                   PickCommentRecommendRepository pickCommentRecommendRepository) {
        super(embeddingsService, pickBestCommentsPolicy, pickPopularScorePolicy, timeProvider, pickRepository,
                pickCommentRepository, pickCommentRecommendRepository);
    }

    @Override
    public PickCommentResponse registerPickComment(Long pickId, PickCommentDto pickCommentDto, Authentication authentication) {

        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public PickCommentResponse registerPickRepliedComment(Long pickParentCommentId, Long pickCommentOriginParentId,
                                                          Long pickId, PickCommentDto pickCommentDto,
                                                          Authentication authentication) {

        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public PickCommentResponse modifyPickComment(Long pickCommentId, Long pickId,
                                                 PickCommentDto pickModifyCommentDto,
                                                 Authentication authentication) {

        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public PickCommentResponse deletePickComment(Long pickCommentId, Long pickId, Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    /**
     * @Note: 정렬 조건에 따라서 커서 방식으로 픽픽픽 댓글/답글을 조회한다.
     * @Author: 장세웅
     * @Since: 2024.10.02
     */
    @Override
    public SliceCustom<PickCommentsResponse> findPickComments(Pageable pageable, Long pickId, Long pickCommentId,
                                                              PickCommentSort pickCommentSort,
                                                              EnumSet<PickOptionType> pickOptionTypes,
                                                              String anonymousMemberId,
                                                              Authentication authentication) {

        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 픽픽픽 댓글/답글 조회
        return super.findPickComments(pageable, pickId, pickCommentId, pickCommentSort, pickOptionTypes, null, null);
    }

    @Override
    public PickCommentRecommendResponse recommendPickComment(Long pickId, Long pickCommendId,
                                                             Authentication authentication) {

        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    /**
     * @Note: 익명회윈이 픽픽픽 베스트 댓글을 조회한다.
     * @Author: 장세웅
     * @Since: 2024.10.09
     */
    @Override
    public List<PickCommentsResponse> findPickBestComments(int size, Long pickId, String anonymousMemberId,
                                                           Authentication authentication) {
        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        return super.findPickBestComments(size, pickId, null, null);
    }
}
