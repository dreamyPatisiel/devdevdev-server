package com.dreamypatisiel.devdevdev.domain.service.notification;

import static com.dreamypatisiel.devdevdev.domain.service.notification.NotificationService.ACCESS_DENIED_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.domain.entity.Subscription;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.exception.NotificationExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.notification.NotificationRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.SubscriptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticle;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticleRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationReadResponse;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class NotificationServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    NotificationService notificationService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    SubscriptionRepository subscriptionRepository;
    @Autowired
    TechArticleRepository techArticleRepository;

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

    @Test
    @DisplayName("회원이 단건 알림을 읽으면 isRead가 true로 변경된다.")
    void readNotification() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 알림 생성
        Notification notification = createNotification(member, "새로운 소식이 도착했습니다.", NotificationType.SUBSCRIPTION, false);
        notificationRepository.save(notification);

        // when
        NotificationReadResponse notificationReadResponse = notificationService.readNotification(notification.getId(),
                authentication);

        em.flush();
        em.clear(); // 영속성 컨텍스트 초기화

        // then
        Notification findNotification = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(findNotification.getIsRead()).isTrue();

        assertAll(
                () -> assertThat(notificationReadResponse.getId()).isEqualTo(notification.getId()),
                () -> assertThat(notificationReadResponse.getIsRead()).isTrue()
        );
    }

    @Test
    @DisplayName("회원이 자신의 알림이 아닌 알림을 조회하면 예외가 발생한다.")
    void readNotificationNotOwnerException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Member owner = Member.createMemberBy(
                createSocialDto("owner", name, nickname, password, "owner@dreamy5patisiel.com", socialType, role));
        memberRepository.save(owner);

        // 알림 생성
        Notification notification = createNotification(owner, "새로운 소식이 도착했습니다.", NotificationType.SUBSCRIPTION, false);
        notificationRepository.save(notification);

        // when // then
        assertThatThrownBy(() -> notificationService.readNotification(notification.getId(), authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(NotificationExceptionMessage.NOT_FOUND_NOTIFICATION_MESSAGE);
    }

    @Test
    @DisplayName("회원이 모든 알림을 읽으면 isRead가 true로 일괄 업데이트된다.")
    void readAllNotifications() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(
                "dreamy", "꿈빛파티시엘", "행복한 꿈빛", "pass123", "dreamy@kakao.com", "KAKAO", "ROLE_USER"
        );
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal principal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(
                principal, principal.getAuthorities(), principal.getSocialType().name()
        ));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 안 읽은 알림 여러 개 저장
        notificationRepository.saveAll(List.of(
                createNotification(member, false),
                createNotification(member, false),
                createNotification(member, false)
        ));

        // when
        notificationService.readAllNotifications(authentication);

        em.flush();
        em.clear();

        // then
        List<Notification> allNotifications = notificationRepository.findAllByMemberId(member.getId());
        assertThat(allNotifications)
                .hasSize(3)
                .allMatch(Notification::getIsRead);
    }

    @Test
    @DisplayName("회원이 읽을 알림이 하나도 없어도 readAllNotifications는 성공한다.")
    void readAllNotificationsWhenNoUnreadNotifications() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(
                "dreamy", "꿈빛파티시엘", "행복한 꿈빛", "pass123", "dreamy@kakao.com", "KAKAO", "ROLE_USER"
        );
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal principal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(
                principal, principal.getAuthorities(), principal.getSocialType().name()
        ));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 읽은 알림만 저장
        notificationRepository.saveAll(List.of(
                createNotification(member, true),
                createNotification(member, true)
        ));

        // when
        notificationService.readAllNotifications(authentication);

        em.flush();
        em.clear();

        // then
        List<Notification> allNotifications = notificationRepository.findAllByMemberId(member.getId());
        assertThat(allNotifications)
                .hasSize(2)
                .allMatch(Notification::getIsRead); // 여전히 모두 읽음 상태
    }

    @Test
    @DisplayName("구독자에게 메인 알림을 전송할 때 알림이 없으면 알림 이력을 저장하고 전송한다.")
    void sendMainTechArticleNotifications() {
        // given
        // 회원 생성
        Member member = createMember();
        memberRepository.save(member);

        // 회사 생성
        Company company = createCompany("트이다");
        companyRepository.save(company);

        // 기술블로그 생성
        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);

        // 기업 구독 생성
        Subscription subscription = createSubscription(company, member);
        subscriptionRepository.save(subscription);

        // 기술블로그 발행 요청 생성
        PublishTechArticleRequest publishTechArticleRequest = new PublishTechArticleRequest(
                company.getId(),
                List.of(new PublishTechArticle(techArticle.getId())));

        // when // then
        assertThatCode(() -> notificationService.sendMainTechArticleNotifications(publishTechArticleRequest))
                .doesNotThrowAnyException();

        // 알림 이력 확인
        List<Notification> findNotifications = notificationRepository.findByMemberInAndTechArticleIdInOrderByNull(
                Set.of(member), Set.of(techArticle.getId()));
        Notification notification = findNotifications.get(0);
        assertAll(
                () -> AssertionsForClassTypes.assertThat(notification.getMember()).isEqualTo(member),
                () -> AssertionsForClassTypes.assertThat(notification.getTechArticle().getId())
                        .isEqualTo(techArticle.getId()),
                () -> AssertionsForClassTypes.assertThat(notification.getMessage()).isNotBlank(),
                () -> AssertionsForClassTypes.assertThat(notification.getIsRead()).isFalse()
        );
    }

    @Test
    @DisplayName("구독자에게 메인 알림을 전송할 때 기업을 구독한 회원이 없으면 알림 이력을 저장하지 않고 알림도 전송하지 않는다.")
    void sendMainTechArticleNotificationsSubscriptionIsEmpty() {
        // given
        // 회원 생성
        Member member = createMember();
        memberRepository.save(member);

        // 회사 생성
        Company company = createCompany("트이다");
        companyRepository.save(company);

        // 기술블로그 생성
        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);

        // 기술블로그 발행 요청 생성
        PublishTechArticleRequest publishTechArticleRequest = new PublishTechArticleRequest(
                company.getId(),
                List.of(new PublishTechArticle(techArticle.getId())));

        // when // then
        assertThatCode(() -> notificationService.sendMainTechArticleNotifications(publishTechArticleRequest))
                .doesNotThrowAnyException();

        // 알림 이력 확인
        List<Notification> findNotifications = notificationRepository.findByMemberInAndTechArticleIdInOrderByNull(
                Set.of(member), Set.of(techArticle.getId()));
        AssertionsForInterfaceTypes.assertThat(findNotifications).isEmpty();
    }

    @Test
    @DisplayName("알림을 생성한다.")
    void publish() {
        // given
        // 회사 생성
        Company company = createCompany("트이다");
        companyRepository.save(company);

        // 기술블로그 생성
        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);

        // 기술블로그 발행 요청 생성
        PublishTechArticleRequest publishTechArticleRequest = new PublishTechArticleRequest(
                company.getId(), List.of(new PublishTechArticle(techArticle.getId())));

        // when
        Long publish = notificationService.publish(NotificationType.SUBSCRIPTION, publishTechArticleRequest);

        // then
        AssertionsForClassTypes.assertThat(publish).isGreaterThan(0);
    }

    @Disabled
    @Test
    @DisplayName("알림을 생성 할 때 회원이 어드민 권한이 아니면 예외가 발생한다.")
    void publishIsNotAdmin() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType,
                Role.ROLE_USER.name());
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 회사 생성
        Company company = createCompany("트이다");
        companyRepository.save(company);

        // 기술블로그 생성
        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);

        // 기술블로그 발행 요청 생성
        PublishTechArticleRequest publishTechArticleRequest = new PublishTechArticleRequest(
                company.getId(), List.of(new PublishTechArticle(techArticle.getId())));

        // when // then
        assertThatThrownBy(
                () -> notificationService.publish(NotificationType.SUBSCRIPTION,
                        publishTechArticleRequest))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(ACCESS_DENIED_MESSAGE);
    }

    private static Subscription createSubscription(Company company, Member member) {
        return Subscription.builder()
                .company(company)
                .member(member)
                .build();
    }

    private static Member createMember() {
        return Member.builder()
                .isDeleted(false)
                .build();
    }

    private static TechArticle createTechArticle(Company company) {
        return TechArticle.builder()
                .company(company)
                .build();
    }

    private static Company createCompany(String name) {
        return Company.builder()
                .name(new CompanyName(name))
                .build();
    }

    private Notification createNotification(Member member, boolean isRead) {
        return Notification.builder()
                .member(member)
                .message("테스트 알림")
                .type(NotificationType.SUBSCRIPTION)
                .isRead(isRead)
                .build();
    }

    private Notification createNotification(Member member, String message, NotificationType type, boolean isRead) {
        return Notification.builder()
                .member(member)
                .message(message)
                .type(type)
                .isRead(isRead)
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