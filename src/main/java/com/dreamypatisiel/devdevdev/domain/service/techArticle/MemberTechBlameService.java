package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import static com.dreamypatisiel.devdevdev.domain.exception.CommonExceptionMessage.INVALID_NOT_FOUND_BLAME_TYPE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_CAN_NOT_ACTION_DELETED_TECH_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.service.common.MemberBlameService.BLAME_TYPE_ETC;
import static com.dreamypatisiel.devdevdev.domain.service.techArticle.TechArticleCommonService.validateIsDeletedTechComment;

import com.dreamypatisiel.devdevdev.domain.entity.Blame;
import com.dreamypatisiel.devdevdev.domain.entity.BlameType;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.repository.BlameTypeRepository;
import com.dreamypatisiel.devdevdev.domain.repository.blame.BlameRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.service.common.dto.BlameTechArticleDto;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.web.dto.response.common.BlameResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberTechBlameService {

    public static final String BLAME = "신고";

    private final TechCommentRepository techCommentRepository;
    private final BlameTypeRepository blameTypeRepository;
    private final BlameRepository blameRepository;

    /**
     * @Note: 기술 블로그 댓글에 신고한다.
     * @Author: 장세웅
     * @Since: 2024.09.17
     */
    @Transactional
    public BlameResponse blameTechArticleComment(BlameTechArticleDto blameTechArticleDto, Member member) {

        Long techArticleId = blameTechArticleDto.getTechArticleId();
        Long techArticleCommentId = blameTechArticleDto.getTechArticleCommentId();
        Long blameTypeId = blameTechArticleDto.getBlameTypeId();
        String customReason = blameTechArticleDto.getCustomReason();

        // 기술블로그 댓글 조회(기술블로그 페치 조인)
        TechComment findTechComment = techCommentRepository.findWithTechArticleByIdAndTechArticleId(
                        techArticleCommentId, techArticleId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE));

        // 기술블로그 댓글 삭제 여부 검증
        validateIsDeletedTechComment(findTechComment, INVALID_CAN_NOT_ACTION_DELETED_TECH_COMMENT_MESSAGE, BLAME);

        // 기술블로그 댓글 신고 사유 조회
        BlameType findBlameType = blameTypeRepository.findById(blameTypeId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_BLAME_TYPE_MESSAGE));

        return createAndGetBlameResponse(member, findBlameType, findTechComment, customReason);
    }

    private BlameResponse createAndGetBlameResponse(Member member, BlameType findBlameType, TechComment techComment,
                                                    String customReason) {
        // 기술 블로그 신고 사유가 기타 이면
        if (findBlameType.isEqualsReason(BLAME_TYPE_ETC)) {
            return createAndSaveBlameTechCommentWithCustomReason(member, findBlameType, techComment, customReason);
        }

        return createAndSaveBlameTechComment(member, findBlameType, techComment);
    }

    private BlameResponse createAndSaveBlameTechComment(Member member, BlameType findBlameType,
                                                        TechComment techComment) {
        Blame blameTechComment = Blame.createBlameTechComment(
                techComment.getTechArticle(), techComment, member, findBlameType);
        blameRepository.save(blameTechComment);

        // 기술블록 댓글 신고 횟수 증가
        techComment.incrementBlameTotalCount();

        return new BlameResponse(blameTechComment.getId());
    }

    private BlameResponse createAndSaveBlameTechCommentWithCustomReason(Member member, BlameType findBlameType,
                                                                        TechComment techComment, String customReason) {
        Blame blameTechCommentWithCustomReason = Blame.createBlameTechCommentWithCustomReason(
                techComment.getTechArticle(), techComment, member, findBlameType, customReason);
        blameRepository.save(blameTechCommentWithCustomReason);

        // 기술블록 댓글 신고 횟수 증가
        techComment.incrementBlameTotalCount();

        return new BlameResponse(blameTechCommentWithCustomReason.getId());
    }
}
