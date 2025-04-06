package com.dreamypatisiel.devdevdev.domain.service.notification;

import static com.dreamypatisiel.devdevdev.domain.service.notification.NotificationService.ACCESS_DENIED_MESSAGE;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
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
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.notification.NotificationRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.SubscriptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticle;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticleRequest;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class NotificationServiceTest {

    @Autowired
    NotificationService notificationService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    SubscriptionRepository subscriptionRepository;
    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    RedisTemplate<?, ?> redisTemplate;

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();

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
        List<Notification> findNotifications = notificationRepository.findByMemberInAndTechArticleIdInOrderByMemberDesc(
                Set.of(member), Set.of(techArticle.getId()));
        Notification notification = findNotifications.get(0);
        assertAll(
                () -> assertThat(notification.getMember()).isEqualTo(member),
                () -> assertThat(notification.getTechArticle().getId()).isEqualTo(techArticle.getId()),
                () -> assertThat(notification.getMessage()).isNotBlank(),
                () -> assertThat(notification.getIsRead()).isFalse()
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
        List<Notification> findNotifications = notificationRepository.findByMemberInAndTechArticleIdInOrderByMemberDesc(
                Set.of(member), Set.of(techArticle.getId()));
        assertThat(findNotifications).isEmpty();
    }

    @Test
    @DisplayName("어드민이 알림을 생성한다.")
    void publishIsAdmin() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType,
                Role.ROLE_ADMIN.name());
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

        // when
        Long publish = notificationService.publish(authentication, NotificationType.SUBSCRIPTION, publishTechArticleRequest);

        // then
        assertThat(publish).isGreaterThan(0);
    }

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
                () -> notificationService.publish(authentication, NotificationType.SUBSCRIPTION, publishTechArticleRequest))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(ACCESS_DENIED_MESSAGE);
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
}
