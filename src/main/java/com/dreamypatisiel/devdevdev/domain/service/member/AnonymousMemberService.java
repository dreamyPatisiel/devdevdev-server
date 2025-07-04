package com.dreamypatisiel.devdevdev.domain.service.member;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_ANONYMOUS_MEMBER_ID_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.repository.member.AnonymousMemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnonymousMemberService {
    
    private final AnonymousMemberRepository anonymousMemberRepository;

    @Transactional
    public AnonymousMember findOrCreateAnonymousMember(String anonymousMemberId) {
        // 익명 사용자 검증
        validateAnonymousMemberId(anonymousMemberId);

        // 익명회원 조회
        Optional<AnonymousMember> optionalAnonymousMember = anonymousMemberRepository.findByAnonymousMemberId(anonymousMemberId);

        // 익명 사용자 닉네임 생성
        String anonymousNickName = "익명의 댑댑이 " + System.nanoTime() % 100_000L;

        // 익명 사용자가 존재하지 않으면
        if (optionalAnonymousMember.isEmpty()) {
            // 익명 사용자 생성
            AnonymousMember anonymousMember = AnonymousMember.create(anonymousMemberId, anonymousNickName);
            return anonymousMemberRepository.save(anonymousMember);
        }

        AnonymousMember anonymousMember = optionalAnonymousMember.get();

        // 익명 사용자가 존재하지만 닉네임이 없다면
        if (!anonymousMember.hasNickName()) {
            anonymousMember.changeNickname(anonymousNickName);
        }

        return anonymousMember;
    }

    private void validateAnonymousMemberId(String anonymousMemberId) {
        if (!StringUtils.hasText(anonymousMemberId)) {
            throw new IllegalArgumentException(INVALID_ANONYMOUS_MEMBER_ID_MESSAGE);
        }
    }
}
