package com.dreamypatisiel.devdevdev.domain.service.blame;

import static com.dreamypatisiel.devdevdev.domain.exception.CommonExceptionMessage.INVALID_ALREADY_EXIST_BLAME;
import static com.dreamypatisiel.devdevdev.domain.exception.CommonExceptionMessage.INVALID_BLAME_PATH_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.repository.BlameTypeRepository;
import com.dreamypatisiel.devdevdev.domain.repository.blame.BlameRepository;
import com.dreamypatisiel.devdevdev.domain.service.blame.dto.BlameDto;
import com.dreamypatisiel.devdevdev.domain.service.blame.dto.BlamePickDto;
import com.dreamypatisiel.devdevdev.domain.service.blame.dto.BlameTechArticleDto;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.web.dto.request.common.BlamePathType;
import com.dreamypatisiel.devdevdev.web.dto.request.common.BlameRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.common.BlameResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.common.BlameTypeResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberBlameService {

    public static final String BLAME_TYPE_ETC = "기타";

    private final MemberProvider memberProvider;
    private final MemberPickBlameService memberPickBlameService;
    private final MemberTechBlameService memberTechBlameService;

    private final BlameTypeRepository blameTypeRepository;
    private final BlameRepository blameRepository;

    /**
     * @Note: 신고 사유를 조회합니다.
     * @Author: 장세웅
     * @Since: 2024.09.11
     */
    public List<BlameTypeResponse> findBlameType() {
        return blameTypeRepository.findAllByOrderBySortOrderAsc().stream()
                .map(BlameTypeResponse::from)
                .toList();
    }

    /**
     * @Note: 댑댑댑에 사용자가 게시한 게시글 또는 댓글을 신고합니다. 이미 신고한 이력이 있으면 예외가 발생합니다.
     * @Author: 장세웅
     * @Since: 2024.09.12
     */
    @Transactional
    public BlameResponse blame(BlamePathType blamePathType, BlameRequest blameRequest, Authentication authentication) {

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 신고 이력 검증
        validateBlame(blameRequest, findMember);

        // 픽픽픽 신고 서비스
        if (blamePathType.equals(BlamePathType.PICK)) {
            // 픽픽픽 전용 dto 생성
            BlamePickDto blamePickDto = BlamePickDto.create(blameRequest);

            // 댓글 아이디가 없으면
            if (blamePickDto.isNullPickCommentId()) {
                // 픽픽픽 신고 수행
                return memberPickBlameService.blamePick(blamePickDto, findMember);
            }

            // 픽픽픽 댓글 신고 수행
            return memberPickBlameService.blamePickComment(blamePickDto, findMember);
        }

        // 기술 블로그 댓글 신고 서비스
        if (blamePathType.equals(BlamePathType.TECH_ARTICLE)) {
            // 기술블로그 전용 dto 생성
            BlameTechArticleDto blameTechArticleDto = BlameTechArticleDto.create(blameRequest);
            return memberTechBlameService.blameTechArticleComment(blameTechArticleDto, findMember);
        }

        throw new IllegalArgumentException(INVALID_BLAME_PATH_MESSAGE);
    }

    private void validateBlame(BlameRequest blameRequest, Member findMember) {
        // 신고 이력 조회
        BlameDto blameDto = BlameDto.of(findMember.getId(), blameRequest);
        Boolean existsBlame = blameRepository.existsBlameByBlameDto(blameDto);

        // 신고 이력이 존재하면
        if (existsBlame) {
            throw new IllegalArgumentException(INVALID_ALREADY_EXIST_BLAME);
        }
    }
}
