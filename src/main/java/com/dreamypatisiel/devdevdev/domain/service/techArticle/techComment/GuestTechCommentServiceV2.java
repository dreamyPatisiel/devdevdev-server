package com.dreamypatisiel.devdevdev.domain.service.techArticle.techComment;

import static com.dreamypatisiel.devdevdev.domain.exception.GuestExceptionMessage.INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.policy.TechBestCommentsPolicy;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentSort;
import com.dreamypatisiel.devdevdev.domain.service.member.AnonymousMemberService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.dto.TechCommentDto;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.TechArticleCommonService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.SliceCommentCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.ModifyTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.RegisterTechCommentRequest;
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
public class GuestTechCommentServiceV2 extends TechCommentCommonService implements TechCommentService {

    private final AnonymousMemberService anonymousMemberService;
    private final TechCommentCommonService techCommentCommonService;
    private final TechArticleCommonService techArticleCommonService;

    public GuestTechCommentServiceV2(TechCommentRepository techCommentRepository, TechBestCommentsPolicy techBestCommentsPolicy,
                                     AnonymousMemberService anonymousMemberService,
                                     TechCommentCommonService techCommentCommonService,
                                     TechArticleCommonService techArticleCommonService) {
        super(techCommentRepository, techBestCommentsPolicy);
        this.anonymousMemberService = anonymousMemberService;
        this.techCommentCommonService = techCommentCommonService;
        this.techArticleCommonService = techArticleCommonService;
    }

    @Override
    @Transactional
    public TechCommentResponse registerMainTechComment(Long techArticleId, TechCommentDto techCommentDto,
                                                       Authentication authentication) {

        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        String anonymousMemberId = techCommentDto.getAnonymousMemberId();
        String contents = techCommentDto.getContents();

        // 회원 조회 또는 생성
        AnonymousMember findAnonymousMember = anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);

        // 기술블로그 조회
        TechArticle techArticle = techArticleCommonService.findTechArticle(techArticleId);

        // 댓글 엔티티 생성 및 저장
        TechComment techComment = TechComment.createMainTechCommentByAnonymousMember(new CommentContents(contents),
                findAnonymousMember, techArticle);
        techCommentRepository.save(techComment);

        // 기술블로그 댓글수 증가
        techArticle.incrementCommentCount();

        // 데이터 가공
        return new TechCommentResponse(techComment.getId());
    }

    @Override
    public TechCommentResponse registerRepliedTechComment(Long techArticleId, Long originParentTechCommentId,
                                                          Long parentTechCommentId,
                                                          RegisterTechCommentRequest registerRepliedTechCommentRequest,
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

    /**
     * @Note: 익명 회원이 기술블로그 댓글/답글을 조회한다.
     * @Author: 장세웅
     * @Since: 2025.07.20
     */
    @Override
    public SliceCommentCustom<TechCommentsResponse> getTechComments(Long techArticleId, Long techCommentId,
                                                                    TechCommentSort techCommentSort, Pageable pageable,
                                                                    String anonymousMemberId,
                                                                    Authentication authentication) {
        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 익명회원 추출
        AnonymousMember anonymousMember = anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);

        // 기술블로그 댓글/답글 조회
        return super.getTechComments(techArticleId, techCommentId, techCommentSort, pageable, null, anonymousMember);
    }

    @Override
    public TechCommentRecommendResponse recommendTechComment(Long techArticleId, Long techCommentId,
                                                             Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    /**
     * @Note: 익명 회원이 기술블로그 베스트 댓글을 조회한다.
     * @Author: 장세웅
     * @Since: 2025.07.20
     */
    @Override
    @Transactional
    public List<TechCommentsResponse> findTechBestComments(int size, Long techArticleId,
                                                           String anonymousMemberId, Authentication authentication) {

        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 익명회원 추출
        AnonymousMember anonymousMember = anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);

        return super.findTechBestComments(size, techArticleId, null, anonymousMember);
    }
}
