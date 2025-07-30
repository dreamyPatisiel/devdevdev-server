package com.dreamypatisiel.devdevdev.domain.service.techArticle.techComment;

import static com.dreamypatisiel.devdevdev.domain.exception.GuestExceptionMessage.INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.policy.TechArticlePopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.policy.TechBestCommentsPolicy;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentSort;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.dto.TechCommentDto;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.SliceCommentCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.ModifyTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentRecommendResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentsResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GuestTechCommentService extends TechCommentCommonService implements TechCommentService {

    public GuestTechCommentService(TechCommentRepository techCommentRepository,
                                   TechBestCommentsPolicy techBestCommentsPolicy,
                                   TechArticlePopularScorePolicy techArticlePopularScorePolicy) {
        super(techCommentRepository, techBestCommentsPolicy, techArticlePopularScorePolicy);
    }

    @Override
    public TechCommentResponse registerMainTechComment(Long techArticleId,
                                                       TechCommentDto techCommentDto,
                                                       Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public TechCommentResponse registerRepliedTechComment(Long techArticleId, Long originParentTechCommentId,
                                                          Long parentTechCommentId,
                                                          TechCommentDto registerRepliedTechCommentRequest,
                                                          Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public TechCommentResponse modifyTechComment(Long techArticleId, Long techCommentId,
                                                 ModifyTechCommentRequest modifyTechCommentRequest,
                                                 Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public TechCommentResponse deleteTechComment(Long techArticleId, Long techCommentId,
                                                 Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public SliceCommentCustom<TechCommentsResponse> getTechComments(Long techArticleId, Long techCommentId,
                                                                    TechCommentSort techCommentSort, Pageable pageable,
                                                                    String anonymousMemberId, Authentication authentication) {
        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 기술블로그 댓글/답글 조회
        return super.getTechComments(techArticleId, techCommentId, techCommentSort, pageable, null, null);
    }

    @Override
    public TechCommentRecommendResponse recommendTechComment(Long techArticleId, Long techCommentId,
                                                             Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    /**
     * @Note: 익명 회원이 기술블로그 베스트 댓글을 조회한다.
     * @Author: 장세웅
     * @Since: 2024.10.27
     */
    @Override
    public List<TechCommentsResponse> findTechBestComments(int size, Long techArticleId, String anonymousMemberId,
                                                           Authentication authentication) {

        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        return super.findTechBestComments(size, techArticleId, null, null);
    }
}
