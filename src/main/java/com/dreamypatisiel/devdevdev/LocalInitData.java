package com.dreamypatisiel.devdevdev;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile(value = {"local", "dev"})
@RequiredArgsConstructor
public class LocalInitData {

    private final MemberRepository memberRepository;

    public final static String nickname = "댑댑이";
    public final static String email = "test@devdevdev.com";
    public final static Role role = Role.ROLE_USER;
    public final static SocialType socialType = SocialType.KAKAO;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("LocalInitData.init()");
        SocialMemberDto socialMemberDto = SocialMemberDto.of(email,socialType.name(), role.name(), nickname);
        memberRepository.save(Member.createMemberBy(socialMemberDto));
    }
}