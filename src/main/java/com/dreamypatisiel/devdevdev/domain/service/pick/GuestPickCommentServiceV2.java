package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.GuestExceptionMessage.INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_VOTE_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.policy.PickBestCommentsPolicy;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.service.member.AnonymousMemberService;
import com.dreamypatisiel.devdevdev.domain.service.pick.dto.PickCommentDto;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
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
public class GuestPickCommentServiceV2 extends PickCommonService implements PickCommentService {

    private final AnonymousMemberService anonymousMemberService;

    private final PickVoteRepository pickVoteRepository;

    public GuestPickCommentServiceV2(EmbeddingsService embeddingsService,
                                     PickBestCommentsPolicy pickBestCommentsPolicy,
                                     TimeProvider timeProvider,
                                     PickRepository pickRepository,
                                     PickCommentRepository pickCommentRepository,
                                     PickCommentRecommendRepository pickCommentRecommendRepository,
                                     AnonymousMemberService anonymousMemberService,
                                     PickPopularScorePolicy pickPopularScorePolicy,
                                     PickVoteRepository pickVoteRepository) {
        super(embeddingsService, pickBestCommentsPolicy, pickPopularScorePolicy, timeProvider, pickRepository,
                pickCommentRepository, pickCommentRecommendRepository);
        this.anonymousMemberService = anonymousMemberService;
        this.pickVoteRepository = pickVoteRepository;
    }

    @Override
    @Transactional
    public PickCommentResponse registerPickComment(Long pickId, PickCommentDto pickCommentDto, Authentication authentication) {

        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        String anonymousMemberId = pickCommentDto.getAnonymousMemberId();
        String contents = pickCommentDto.getContents();
        Boolean isPickVotePublic = pickCommentDto.getIsPickVotePublic();

        // 익명 회원 추출
        AnonymousMember anonymousMember = anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);

        // 픽픽픽 조회
        Pick findPick = pickRepository.findById(pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_MESSAGE));

        // 댓글 갯수 증가 및 인기점수 반영
        findPick.incrementCommentTotalCount();
        findPick.changePopularScore(pickPopularScorePolicy);

        // 픽픽픽 게시글의 승인 상태 검증
        validateIsApprovalPickContentStatus(findPick, INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE, REGISTER);

        // 픽픽픽 선택지 투표 공개인 경우
        if (isPickVotePublic) {
            // 익명회원이 투표한 픽픽픽 투표 조회
            PickVote findPickVote = pickVoteRepository.findWithPickAndPickOptionByPickIdAndAnonymousMemberAndDeletedAtIsNull(
                            pickId, anonymousMember)
                    .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_VOTE_MESSAGE));

            // 픽픽픽 투표한 픽 옵션의 댓글 작성
            PickComment pickComment = PickComment.createPublicVoteCommentByAnonymousMember(new CommentContents(contents),
                    anonymousMember, findPick, findPickVote);
            pickCommentRepository.save(pickComment);

            return new PickCommentResponse(pickComment.getId());
        }

        // 픽픽픽 선택지 투표 비공개인 경우
        PickComment pickComment = PickComment.createPrivateVoteCommentByAnonymousMember(new CommentContents(contents),
                anonymousMember, findPick);
        pickCommentRepository.save(pickComment);

        return new PickCommentResponse(pickComment.getId());
    }

    @Override
    @Transactional
    public PickCommentResponse registerPickRepliedComment(Long pickParentCommentId, Long pickCommentOriginParentId,
                                                          Long pickId, PickCommentDto pickRegisterRepliedCommentDto,
                                                          Authentication authentication) {

        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        String contents = pickRegisterRepliedCommentDto.getContents();
        String anonymousMemberId = pickRegisterRepliedCommentDto.getAnonymousMemberId();

        // 익명 회원 추출
        AnonymousMember anonymousMember = anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);

        // 픽픽픽 댓글 로직 수행
        PickReplyContext pickReplyContext = prepareForReplyRegistration(pickParentCommentId, pickCommentOriginParentId, pickId);

        PickComment findParentPickComment = pickReplyContext.parentPickComment();
        PickComment findOriginParentPickComment = pickReplyContext.originParentPickComment();
        Pick findPick = pickReplyContext.pick();

        // 픽픽픽 서브 댓글(답글) 생성
        PickComment pickRepliedComment = PickComment.createRepliedCommentByAnonymousMember(new CommentContents(contents),
                findParentPickComment, findOriginParentPickComment, anonymousMember, findPick);
        pickCommentRepository.save(pickRepliedComment);

        return new PickCommentResponse(pickRepliedComment.getId());
    }

    @Override
    public PickCommentResponse modifyPickComment(Long pickCommentId, Long pickId, PickCommentDto pickCommentDto,
                                                 Authentication authentication) {

        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        String contents = pickCommentDto.getContents();
        String anonymousMemberId = pickCommentDto.getAnonymousMemberId();

        // 익명 회원 추출
        AnonymousMember anonymousMember = anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);

        // 픽픽픽 댓글 조회(익명 회원 본인이 댓글 작성, 삭제되지 않은 댓글)
        PickComment findPickComment = pickCommentRepository.findWithPickByIdAndPickIdAndCreatedAnonymousByIdAndDeletedAtIsNull(
                        pickCommentId, pickId, anonymousMember.getId())
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE));

        // 픽픽픽 게시글의 승인 상태 검증
        validateIsApprovalPickContentStatus(findPickComment.getPick(), INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE,
                MODIFY);

        // 댓글 수정
        findPickComment.modifyCommentContents(new CommentContents(contents), timeProvider.getLocalDateTimeNow());

        return new PickCommentResponse(findPickComment.getId());
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
                                                              Authentication authentication) {

        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 픽픽픽 댓글/답글 조회
        return super.findPickComments(pageable, pickId, pickCommentId, pickCommentSort, pickOptionTypes, null);
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
    public List<PickCommentsResponse> findPickBestComments(int size, Long pickId,
                                                           Authentication authentication) {
        // 익명 회원인지 검증
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        return super.findPickBestComments(size, pickId, null);
    }
}
