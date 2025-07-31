package com.dreamypatisiel.devdevdev.domain.service.techArticle.techComment;

import static com.dreamypatisiel.devdevdev.domain.exception.GuestExceptionMessage.INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.policy.TechArticlePopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.policy.TechBestCommentsPolicy;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentSort;
import com.dreamypatisiel.devdevdev.domain.service.member.AnonymousMemberService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.dto.TechCommentDto;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.TechArticleCommonService;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.SliceCommentCustom;
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

    private final TimeProvider timeProvider;

    private final AnonymousMemberService anonymousMemberService;
    private final TechArticleCommonService techArticleCommonService;

    public GuestTechCommentServiceV2(TimeProvider timeProvider, TechCommentRepository techCommentRepository,
                                     TechBestCommentsPolicy techBestCommentsPolicy,
                                     AnonymousMemberService anonymousMemberService,
                                     TechArticlePopularScorePolicy techArticlePopularScorePolicy,
                                     TechArticleCommonService techArticleCommonService) {
        super(techCommentRepository, techBestCommentsPolicy, techArticlePopularScorePolicy);
        this.timeProvider = timeProvider;
        this.anonymousMemberService = anonymousMemberService;
        this.techArticleCommonService = techArticleCommonService;
    }

    @Override
    @Transactional
    public TechCommentResponse registerMainTechComment(Long techArticleId, TechCommentDto registerTechCommentDto,
                                                       Authentication authentication) {

        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        String anonymousMemberId = registerTechCommentDto.getAnonymousMemberId();
        String contents = registerTechCommentDto.getContents();

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
    @Transactional
    public TechCommentResponse registerRepliedTechComment(Long techArticleId, Long originParentTechCommentId,
                                                          Long parentTechCommentId,
                                                          TechCommentDto registerRepliedTechCommentDto,
                                                          Authentication authentication) {
        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        String anonymousMemberId = registerRepliedTechCommentDto.getAnonymousMemberId();
        String contents = registerRepliedTechCommentDto.getContents();

        // 회원 조회 또는 생성
        AnonymousMember findAnonymousMember = anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);

        // 답글 대상의 기술블로그 댓글 조회
        TechComment findParentTechComment = techCommentRepository.findWithTechArticleByIdAndTechArticleId(
                        parentTechCommentId, techArticleId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE));

        // 답글 엔티티 생성 및 저장
        TechComment findOriginParentTechComment = super.getAndValidateOriginParentTechComment(originParentTechCommentId,
                findParentTechComment);
        TechArticle findTechArticle = findParentTechComment.getTechArticle();

        TechComment repliedTechComment = TechComment.createRepliedTechCommentByAnonymousMember(new CommentContents(contents),
                findAnonymousMember, findTechArticle, findOriginParentTechComment, findParentTechComment);
        techCommentRepository.save(repliedTechComment);

        // 아티클의 댓글수 증가
        findTechArticle.incrementCommentCount();
        findTechArticle.changePopularScore(techArticlePopularScorePolicy);

        // origin 댓글의 답글수 증가
        findOriginParentTechComment.incrementReplyTotalCount();

        // 데이터 가공
        return new TechCommentResponse(repliedTechComment.getId());
    }

    @Override
    @Transactional
    public TechCommentResponse modifyTechComment(Long techArticleId, Long techCommentId, TechCommentDto modifyTechCommentDto,
                                                 Authentication authentication) {
        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        String contents = modifyTechCommentDto.getContents();
        String anonymousMemberId = modifyTechCommentDto.getAnonymousMemberId();

        // 회원 조회 또는 생성
        AnonymousMember findAnonymousMember = anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);

        // 기술블로그 댓글 조회
        TechComment findTechComment = techCommentRepository.findByIdAndTechArticleIdAndCreatedAnonymousByAndDeletedAtIsNull(
                        techCommentId, techArticleId, findAnonymousMember)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE));

        // 댓글 수정
        findTechComment.modifyCommentContents(new CommentContents(contents), timeProvider.getLocalDateTimeNow());

        // 데이터 가공
        return new TechCommentResponse(findTechComment.getId());
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
