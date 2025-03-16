package com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Subscription;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.exception.CompanyExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.exception.SubscriptionExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.SubscriptionRepository;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.SubscriptionException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.SubscriptionResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class SubscriptionServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    SubscriptionService subscriptionService;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    SubscriptionRepository subscriptionRepository;

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

    @Test
    @DisplayName("회원이 기업을 구독한다.")
    void subscribe() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 기업 생성
        Company company = createCompany("teuida");
        companyRepository.save(company);

        // when
        SubscriptionResponse subscriptionResponse = subscriptionService.subscribe(company.getId(), authentication);

        // then
        assertThat(subscriptionResponse).isNotNull();

        Subscription findSubscription = subscriptionRepository.findById(subscriptionResponse.getId()).get();
        assertAll(
                () -> assertThat(findSubscription.getMember().getId()).isEqualTo(member.getId()),
                () -> assertThat(findSubscription.getCompany().getId()).isEqualTo(company.getId())
        );
    }

    @Test
    @DisplayName("회원이 기업을 구독할 때 회원이 존재하지 않으면 예외가 발생한다.")
    void subscribeMemberException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 기업 생성
        Company company = createCompany("teuida");
        companyRepository.save(company);

        // when // then
        assertThatThrownBy(() -> subscriptionService.subscribe(company.getId(), authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("회원이 기업을 구독할 때 이미 구독한 상태이면 예외가 발생한다.")
    void subscribeSubscriptionException() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 기업 생성
        Company company = createCompany("teuida");
        companyRepository.save(company);

        // 구독 생성
        Subscription subscription = createSubscription(member, company);
        subscriptionRepository.save(subscription);

        // when // then
        assertThatThrownBy(() -> subscriptionService.subscribe(company.getId(), authentication))
                .isInstanceOf(SubscriptionException.class)
                .hasMessage(SubscriptionExceptionMessage.ALREADY_SUBSCRIBED_COMPANY_MESSAGE);
    }

    @Test
    @DisplayName("회원이 기업을 구독할 때 기업이 존재하지 않으면 예외가 발생한다.")
    void subscribeNotFoundException() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(() -> subscriptionService.subscribe(0L, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(CompanyExceptionMessage.NOT_FOUND_COMPANY_MESSAGE);
    }

    @Test
    @DisplayName("회원이 기업 구독을 취소한다.")
    void unsubscribe() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 기업 생성
        Company company = createCompany("teuida");
        companyRepository.save(company);

        // 구독 생성
        Subscription subscription = createSubscription(member, company);
        subscriptionRepository.save(subscription);

        // when // then
        assertThatCode(() -> subscriptionService.unsubscribe(company.getId(), authentication))
                .doesNotThrowAnyException();

        em.flush();
        em.clear();

        Subscription findSubscription = subscriptionRepository.findById(subscription.getId())
                .orElse(null);
        assertThat(findSubscription).isNull();
    }

    @Test
    @DisplayName("회원이 기업 구독을 취소할 때 회원이 존재하지 않으면 예외가 발생한다.")
    void unsubscribeMemberException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 기업 생성
        Company company = createCompany("teuida");
        companyRepository.save(company);

        // when // then
        assertThatThrownBy(() -> subscriptionService.unsubscribe(company.getId(), authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("회원이 기업 구독을 취소할 때 구독 이력이 존재하지 않으면 예외가 발생한다.")
    void unsubscribeNotFoundException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 기업 생성
        Company company = createCompany("teuida");
        companyRepository.save(company);

        // when // then
        assertThatThrownBy(() -> subscriptionService.unsubscribe(company.getId(), authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(SubscriptionExceptionMessage.NOT_FOUND_SUBSCRIPTION_MESSAGE);
    }

    private static Subscription createSubscription(Member member, Company company) {
        return Subscription.builder()
                .member(member)
                .company(company)
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

    private static Company createCompany(String companyName) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .build();
    }
}