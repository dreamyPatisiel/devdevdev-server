package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_VOTE_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.PickCommentResponse;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickCommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberPickCommentService {

    private final MemberProvider memberProvider;
    private final PickRepository pickRepository;
    private final PickVoteRepository pickVoteRepository;
    private final PickCommentRepository pickCommentRepository;

    @Transactional
    public PickCommentResponse registerPickComment(Long pickId, RegisterPickCommentRequest registerPickCommentRequest,
                                                   Authentication authentication) {

        String contents = registerPickCommentRequest.getContents();
        Boolean isPickVotePublic = registerPickCommentRequest.getIsPickVotePublic();

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 픽픽픽 조회
        Pick findPick = pickRepository.findById(pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_MESSAGE));

        // 픽픽픽 게시글의 승인 상태가 아니면
        if (!findPick.isTrueContentStatus(ContentStatus.APPROVAL)) {
            throw new IllegalArgumentException(INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE);
        }

        // 픽픽픽 선택지 투표 공개인 경우
        if (isPickVotePublic) {
            // 회원이 투표한 픽픽픽 투표 조회
            PickVote findPickVote = pickVoteRepository.findWithPickAndPickOptionByPickIdAndMember(pickId, findMember)
                    .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_VOTE_MESSAGE));

            // 픽픽픽 투표한 픽 옵션의 댓글 작성
            PickComment pickComment = PickComment.createPublicVoteComment(new CommentContents(contents), findMember,
                    findPick, findPickVote);

            pickCommentRepository.save(pickComment);

            return new PickCommentResponse(pickComment.getId());
        }

        // 픽픽픽 선택지 투표 비공개인 경우
        PickComment pickComment = PickComment.createPrivateVoteComment(new CommentContents(contents), findMember,
                findPick);
        pickCommentRepository.save(pickComment);

        return new PickCommentResponse(pickComment.getId());
    }
}
