package com.dreamypatisiel.devdevdev.domain.service.notification;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.exception.NotificationExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.notification.NotificationRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationNewArticleResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationPopupNewArticleResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationReadResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

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
    TechArticleRepository techArticleRepository;
    @Autowired
    ElasticTechArticleRepository elasticTechArticleRepository;

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

    @AfterAll
    static void tearDown(@Autowired ElasticTechArticleRepository elasticTechArticleRepository,
                         @Autowired TechArticleRepository techArticleRepository) {
        elasticTechArticleRepository.deleteAll();
        techArticleRepository.deleteAllInBatch();
    }

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
        NotificationReadResponse notificationReadResponse = notificationService.readNotification(notification.getId(), authentication);

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
    @DisplayName("회원이 알림 팝업을 조회하면 최신 5개의 알림이 반환된다.")
    void getNotificationPopup() {
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

        // 알림 6개 저장 (최신순 5개만 반환되는지 확인하기 위해)
        Company company = createCompany("꿈빛 파티시엘", "https://example.com/company.png", "https://example.com",
                "https://example.com");
        companyRepository.save(company);

        List<TechArticle> techArticles = new ArrayList<>();
        List<Notification> notifications = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목 "+i), new Url("https://example.com"),
                    new Count(1L), new Count(1L), new Count(1L), new Count(1L), null, company);

            techArticles.add(techArticle);
            notifications.add(createNotification(member, "알림 메시지 " + i, NotificationType.SUBSCRIPTION, false, techArticle));
        }

        techArticleRepository.saveAll(techArticles);
        notificationRepository.saveAll(notifications);

        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 5);

        // when
        var response = notificationService.getNotificationPopup(pageable, authentication);

        // then
        assertThat(response.getContent()).hasSize(5);
        assertThat(response.getTotalElements()).isEqualTo(6); // 읽지 않은 알림 총 개수

        response.getContent().forEach(popup -> {
            assertThat(popup).isInstanceOf(NotificationPopupNewArticleResponse.class);
            NotificationPopupNewArticleResponse articlePopup = (NotificationPopupNewArticleResponse) popup;
            assertThat(articlePopup.getId()).isNotNull();
            assertThat(articlePopup.getTitle()).isNotNull();
            assertThat(articlePopup.getCreatedAt()).isNotNull();
            assertThat(articlePopup.getCompanyName()).isNotNull();
            assertThat(articlePopup.getTechArticleId()).isNotNull();
            assertThat(articlePopup.getType()).isEqualTo(NotificationType.SUBSCRIPTION);
            assertThat(articlePopup.getIsRead()).isFalse();
        });
    }

    @Test
    @DisplayName("회원이 알림 페이지를 조회하면 알림 목록이 커서 기반으로 반환된다.")
    void getNotifications() {
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

        // 알림 10개 저장
        Company company = createCompany("꿈빛 파티시엘", "https://example.com/company.png", "https://example.com",
                "https://example.com");
        companyRepository.save(company);


        List<ElasticTechArticle> elasticTechArticles = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ElasticTechArticle elasticTechArticle = createElasticTechArticle("elasticId" + i, "기술블로그 제목 "+i,
                    LocalDate.now(), "기술블로그 내용", "https://example.com", "기술블로그 설명",
                    "https://example.com/thumbnail.png", "작성자", "회사명", company.getId(),
                    1L, 1L, 1L, 1L);
            elasticTechArticles.add(elasticTechArticle);
        }
        elasticTechArticleRepository.saveAll(elasticTechArticles);

        List<TechArticle> techArticles = new ArrayList<>();
        List<Notification> notifications = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목 "+i), new Url("https://example.com"),
                    new Count(1L), new Count(1L), new Count(1L), new Count(1L), elasticTechArticles.get(i).getId(), company);

            techArticles.add(techArticle);
            notifications.add(createNotification(member, "알림 메시지 " + i, NotificationType.SUBSCRIPTION, false, techArticle));
        }

        techArticleRepository.saveAll(techArticles);
        notificationRepository.saveAll(notifications);

        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 5);

        // when
        var response = notificationService.getNotifications(pageable, null, authentication);

        // then
        assertThat(response.getContent()).hasSize(pageable.getPageSize());
        assertThat(response.getTotalElements()).isEqualTo(10); // 읽지 않은 알림 총 개수
        assertThat(response.hasNext()).isTrue(); // 다음 페이지가 존재하는지 확인

        response.getContent().forEach(notification -> {
            assertThat(notification).isInstanceOf(NotificationNewArticleResponse.class);
            NotificationNewArticleResponse newArticleResponse = (NotificationNewArticleResponse) notification;
            assertThat(newArticleResponse.getNotificationId()).isNotNull();
            assertThat(newArticleResponse.getType()).isEqualTo(NotificationType.SUBSCRIPTION);
            assertThat(newArticleResponse.getIsRead()).isFalse();
            assertThat(newArticleResponse.getCreatedAt()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getId()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getElasticId()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getThumbnailUrl()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getIsLogoImage()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getTechArticleUrl()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getTitle()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getContents()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getCompany().getId()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getCompany().getName()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getCompany().getCareerUrl()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getCompany().getOfficialImageUrl()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getRegDate()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getViewTotalCount()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getRecommendTotalCount()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getCommentTotalCount()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getPopularScore()).isNotNull();
            assertThat(newArticleResponse.getTechArticle().getIsBookmarked()).isNotNull();
        });
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

    private Notification createNotification(Member member, String message, NotificationType type, boolean isRead,
                                            TechArticle techArticle) {
        return Notification.builder()
                .member(member)
                .message(message)
                .type(type)
                .isRead(isRead)
                .type(NotificationType.SUBSCRIPTION)
                .techArticle(techArticle)
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

    private static Company createCompany(String companyName, String officialImageUrl, String officialUrl,
                                         String careerUrl) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .officialImageUrl(new Url(officialImageUrl))
                .careerUrl(new Url(careerUrl))
                .officialUrl(new Url(officialUrl))
                .build();
    }

    private static ElasticTechArticle createElasticTechArticle(String id, String title, LocalDate regDate,
                                                               String contents, String techArticleUrl,
                                                               String description, String thumbnailUrl, String author,
                                                               String company, Long companyId,
                                                               Long viewTotalCount, Long recommendTotalCount,
                                                               Long commentTotalCount, Long popularScore) {
        return ElasticTechArticle.builder()
                .id(id)
                .title(title)
                .regDate(regDate)
                .contents(contents)
                .techArticleUrl(techArticleUrl)
                .description(description)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .company(company)
                .companyId(companyId)
                .viewTotalCount(viewTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .commentTotalCount(commentTotalCount)
                .popularScore(popularScore)
                .build();
    }
}