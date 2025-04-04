package com.dreamypatisiel.devdevdev.domain.service;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.SseEmitterRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.notification.NotificationRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.redis.sub.NotificationMessageDto;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@SpringBootTest
@Transactional
class SseEmitterServiceTest {

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

    @Autowired
    SseEmitterService sseEmitterService;

    @Autowired
    SseEmitterRepository sseEmitterRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    NotificationRepository notificationRepository;

    @Test
    @DisplayName("회원이 구독자로 추가되었을 때, SseEmitter 객체를 반환한다.")
    void addClient() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when
        SseEmitter sseEmitter = sseEmitterService.addClient(authentication);

        // then
        SseEmitter findSseEmitter = sseEmitterRepository.findByMemberId(member);
        assertThat(sseEmitter).isNotNull();
        assertThat(sseEmitter).isEqualTo(findSseEmitter);
    }

    @Test
    @DisplayName("회원이 구독자로 추가되었을 때, 회원이 존재하지 않으면 예외가 발생한다.")
    void addClientMemberException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(() -> sseEmitterService.addClient(authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("기술블로그 알림 발생시 회원에게 실시간으로 전송한다.")
    void sendNotification() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 회사 생성
        Company company = createCompany("Teuida");
        companyRepository.save(company);

        // 기술 블로그 생성
        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);

        // 기술 블로그 알림 생성
        Notification notification = createTechArticleNotification(member, techArticle, "알림 메시지");
        notificationRepository.save(notification);

        // 구독자 생성
        SseEmitter sseEmitter = new SseEmitter();
        sseEmitterRepository.save(member, sseEmitter);

        // 알림 메시지 생성
        NotificationMessageDto notificationMessageDto = new NotificationMessageDto(notification);

        // when
        sseEmitterService.sendNotification(notificationMessageDto, member);

        // then
        assertThat(sseEmitterRepository.findByMemberId(member)).isEqualTo(sseEmitter);
    }

    @Test
    void sendNotificationIOException() {
        // Given: 테스트 데이터 준비
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 회사 생성
        Company company = createCompany("Teuida");
        companyRepository.save(company);

        // 기술 블로그 생성
        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);

        // 기술 블로그 알림 생성
        Notification notification = createTechArticleNotification(member, techArticle, "알림 메시지");
        notificationRepository.save(notification);

        // SseEmitter를 직접 생성하고 예외를 유발하도록 설정
        SseEmitter sseEmitter = new SseEmitter() {
            @Override
            public void send(SseEventBuilder builder) throws IOException {
                throw new IOException("Test exception");
            }
        };
        sseEmitterRepository.save(member, sseEmitter);

        // 알림 메시지 생성
        NotificationMessageDto notificationMessageDto = new NotificationMessageDto(notification);

        // when
        sseEmitterService.sendNotification(notificationMessageDto, member);

        // then
        assertThat(sseEmitterRepository.findByMemberId(member)).isNull();
    }

    private static Company createCompany(String name) {
        return Company.builder()
                .name(new CompanyName(name))
                .build();
    }

    private static TechArticle createTechArticle(Company company) {
        return TechArticle.builder()
                .company(company)
                .build();
    }

    private static Notification createTechArticleNotification(Member member, TechArticle techArticle, String message) {
        return Notification.builder()
                .member(member)
                .techArticle(techArticle)
                .message(message)
                .type(NotificationType.SUBSCRIPTION)
                .isRead(false)
                .build();
    }

    private SocialMemberDto createSocialDto(String userId, String name, String nickName, String password, String email,
                                            String socialType, String role) {
        return SocialMemberDto.builder()
                .userId(userId)
                .name(name)
                .nickname(nickName)
                .password(password)
                .email(email)
                .socialType(SocialType.valueOf(socialType))
                .role(Role.valueOf(role))
                .build();
    }
}