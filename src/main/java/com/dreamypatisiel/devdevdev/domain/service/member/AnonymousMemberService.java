package com.dreamypatisiel.devdevdev.domain.service.member;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.repository.member.AnonymousMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_ANONYMOUS_MEMBER_ID_MESSAGE;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AnonymousMemberService {

    private final AnonymousMemberRepository anonymousMemberRepository;

    public AnonymousMember findOrCreateAnonymousMember(String anonymousMemberId) {
        // 익명 사용자 검증
        validateAnonymousMemberId(anonymousMemberId);

        // 익명회원 조회 또는 생성
        return anonymousMemberRepository.findByAnonymousMemberId(anonymousMemberId)
                .orElseGet(() -> anonymousMemberRepository.save(AnonymousMember.create(anonymousMemberId)));
    }

    private void validateAnonymousMemberId(String anonymousMemberId) {
        if (!StringUtils.hasText(anonymousMemberId)) {
            throw new IllegalArgumentException(INVALID_ANONYMOUS_MEMBER_ID_MESSAGE);
        }
    }
}
