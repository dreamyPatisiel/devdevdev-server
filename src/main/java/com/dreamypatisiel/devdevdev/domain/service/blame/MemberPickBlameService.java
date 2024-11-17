package com.dreamypatisiel.devdevdev.domain.service.blame;

import static com.dreamypatisiel.devdevdev.domain.entity.Blame.createBlamePickComment;
import static com.dreamypatisiel.devdevdev.domain.exception.CommonExceptionMessage.INVALID_NOT_FOUND_BLAME_TYPE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_CAN_NOT_ACTION_DELETED_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_SERVICE_PATH_ACCESS;
import static com.dreamypatisiel.devdevdev.domain.service.blame.MemberBlameService.BLAME_TYPE_ETC;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickCommonService.validateIsApprovalPickContentStatus;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickCommonService.validateIsDeletedPickComment;

import com.dreamypatisiel.devdevdev.domain.entity.Blame;
import com.dreamypatisiel.devdevdev.domain.entity.BlameType;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.repository.BlameTypeRepository;
import com.dreamypatisiel.devdevdev.domain.repository.blame.BlameRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.service.blame.dto.BlamePickDto;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.web.dto.response.common.BlameResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPickBlameService {

    public static final String BLAME = "신고";

    private final PickRepository pickRepository;
    private final BlameTypeRepository blameTypeRepository;
    private final BlameRepository blameRepository;
    private final PickCommentRepository pickCommentRepository;

    /**
     * @Note: 사용자가 게시한 픽픽픽을 신고합니다.
     * @Author: 장세웅
     * @Since: 2024.09.12
     */
    @Transactional
    public BlameResponse blamePick(BlamePickDto blamePickDto, Member member) {

        Long pickId = blamePickDto.getPickId();
        Long blameTypeId = blamePickDto.getBlameTypeId();
        String customReason = blamePickDto.getCustomReason();

        // 픽픽픽 조회
        Pick findPick = pickRepository.findById(pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_MESSAGE));

        // 픽픽픽 승인상태 검증
        validateIsApprovalPickContentStatus(findPick, INVALID_NOT_FOUND_PICK_MESSAGE, null);

        // 픽픽픽 신고 사유 조회
        BlameType findBlameType = blameTypeRepository.findById(blameTypeId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_BLAME_TYPE_MESSAGE));

        return createAndGetBlameResponse(findBlameType, customReason, findPick, member);
    }

    private BlameResponse createAndGetBlameResponse(BlameType blameType, String customReason,
                                                    Pick pick, Member member) {
        // 픽픽픽 신고 종류가 기타 이면
        if (blameType.isEqualsReason(BLAME_TYPE_ETC)) {
            return createAndSaveBlamePickWithCustomReason(blameType, pick, member, customReason);
        }

        // 픽픽픽 신고 종류가 기타가 아니면
        return createAndSaveBlamePick(blameType, pick, member);
    }

    private BlameResponse createAndSaveBlamePick(BlameType blameType, Pick pick, Member member) {
        // 픽픽픽 신고 생성
        Blame blamePick = Blame.createBlamePick(pick, member, blameType);
        blameRepository.save(blamePick);

        // 픽픽픽 신고 횟수 증가
        pick.incrementBlameTotalCount();

        return new BlameResponse(blamePick.getId());
    }

    private BlameResponse createAndSaveBlamePickWithCustomReason(BlameType blameType, Pick pick, Member member,
                                                                 String customReason) {
        // 픽픽픽 신고 생성
        Blame blamePickWithCustomReason = Blame.createBlamePickWithCustomReason(pick, member, blameType, customReason);
        blameRepository.save(blamePickWithCustomReason);

        // 픽픽픽 신고 횟수 증가
        pick.incrementBlameTotalCount();

        return new BlameResponse(blamePickWithCustomReason.getId());
    }

    /**
     * @Note: 사용자가 게시한 픽픽픽 댓글/답글을 신고합니다.
     * @Author: 장세웅
     * @Since: 2024.09.15
     */
    @Transactional
    public BlameResponse blamePickComment(BlamePickDto blamePickDto, Member member) {

        Long pickId = blamePickDto.getPickId();
        Long pickCommentId = blamePickDto.getPickCommentId();
        Long blameTypeId = blamePickDto.getBlameTypeId();
        String customReason = blamePickDto.getCustomReason();

        // 댓글 아이디가 존재하지 않으면
        if (ObjectUtils.isEmpty(pickCommentId)) {
            throw new IllegalStateException(INVALID_SERVICE_PATH_ACCESS);
        }

        // 픽픽픽 댓글 조회
        PickComment findPickComment = pickCommentRepository.findWithPickByIdAndPickId(pickCommentId, pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE));

        // 픽픽픽 승인 상태 검증(픽픽픽은 물리적 삭제)
        validateIsApprovalPickContentStatus(findPickComment.getPick(), INVALID_NOT_FOUND_PICK_MESSAGE, null);

        // 픽픽픽 댓글 삭제 상태 검증(픽픽픽 댓글은 소프트 삭제)
        validateIsDeletedPickComment(findPickComment, INVALID_CAN_NOT_ACTION_DELETED_PICK_COMMENT_MESSAGE, BLAME);

        // 픽픽픽 신고 사유 조회
        BlameType findBlameType = blameTypeRepository.findById(blameTypeId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_BLAME_TYPE_MESSAGE));

        return createAndGetBlameResponse(member, findBlameType, findPickComment, customReason);
    }

    private BlameResponse createAndGetBlameResponse(Member member, BlameType blameType, PickComment pickComment,
                                                    String customReason) {
        // 픽픽픽 신고 종류가 기타 이면
        if (blameType.isEqualsReason(BLAME_TYPE_ETC)) {
            return createAndSaveBlamePickCommentWithCustomReason(member, blameType, pickComment, customReason);
        }

        return createAndSaveBlamePickComment(member, blameType, pickComment);
    }

    private BlameResponse createAndSaveBlamePickComment(Member member, BlameType blameType, PickComment pickComment) {
        // 픽픽픽 댓글 신고
        Blame blamePickComment = createBlamePickComment(pickComment.getPick(), pickComment, member,
                blameType);
        blameRepository.save(blamePickComment);

        // 픽픽픽 댓글 신고 횟수 증가
        pickComment.incrementBlameTotalCount();

        return new BlameResponse(blamePickComment.getId());
    }

    private BlameResponse createAndSaveBlamePickCommentWithCustomReason(Member member, BlameType blameType,
                                                                        PickComment pickComment, String customReason) {
        // 픽픽픽 댓글 신고
        Blame blamePickCommentWithCustomReason = Blame.createBlamePickCommentWithCustomReason(
                pickComment.getPick(), pickComment, member, blameType, customReason);
        blameRepository.save(blamePickCommentWithCustomReason);

        // 픽픽픽 댓글 신고 횟수 증가
        pickComment.incrementBlameTotalCount();

        return new BlameResponse(blamePickCommentWithCustomReason.getId());
    }
}
