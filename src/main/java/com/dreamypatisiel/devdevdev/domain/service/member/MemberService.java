package com.dreamypatisiel.devdevdev.domain.service.member;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberProvider memberProvider;
    private final MemberRepository memberRepository;
    private final TimeProvider timeProvider;

    /**
     * 회원 탈퇴 회원의 북마크와 회원 정보를 삭제합니다.
     */
    @Transactional
    public void deleteMember(Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 회원 삭제
        member.deleteMember(timeProvider.getLocalDateTimeNow());
    }
}
