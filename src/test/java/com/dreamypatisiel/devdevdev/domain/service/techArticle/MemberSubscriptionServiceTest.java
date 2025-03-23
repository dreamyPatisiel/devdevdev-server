package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Subscription;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.exception.CompanyExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.exception.SubscriptionExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.SubscriptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription.MemberSubscriptionService;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticKeywordRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.SubscriptionException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.web.dto.response.subscription.CompanyDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.subscription.SubscriableCompanyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.SubscriptionResponse;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberSubscriptionServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberSubscriptionService memberSubscriptionService;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    MemberRepository memberRepository;
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
        SubscriptionResponse subscriptionResponse = memberSubscriptionService.subscribe(company.getId(),
                authentication);

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
        assertThatThrownBy(() -> memberSubscriptionService.subscribe(company.getId(), authentication))
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
        assertThatThrownBy(() -> memberSubscriptionService.subscribe(company.getId(), authentication))
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
        assertThatThrownBy(() -> memberSubscriptionService.subscribe(0L, authentication))
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
        assertThatCode(() -> memberSubscriptionService.unsubscribe(company.getId(), authentication))
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
        assertThatThrownBy(() -> memberSubscriptionService.unsubscribe(company.getId(), authentication))
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
        assertThatThrownBy(() -> memberSubscriptionService.unsubscribe(company.getId(), authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(SubscriptionExceptionMessage.NOT_FOUND_SUBSCRIPTION_MESSAGE);
    }

    @Test
    @DisplayName("회원이 구독 가능한 기업 목록을 커서 방식으로 조회한다.")
    void getSubscribableCompany() {
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

        // 회사 생성
        Company company1 = createCompany("teuida1", "https://www.teuida1.net");
        Company company2 = createCompany("teuida2", "https://www.teuida2.net");
        Company company3 = createCompany("teuida3", "https://www.teuida3.net");
        Company company4 = createCompany("teuida4", "https://www.teuida4.net");
        Company company5 = createCompany("teuida5", "https://www.teuida5.net");
        companyRepository.saveAll(List.of(company1, company2, company3, company4, company5));

        // 구독 생성
        Subscription subscription1 = createSubscription(member, company1);
        Subscription subscription2 = createSubscription(member, company2);
        subscriptionRepository.saveAll(List.of(subscription1, subscription2));

        Pageable pageable = PageRequest.of(0, 2);

        // when
        Slice<SubscriableCompanyResponse> subscribableCompany1 = memberSubscriptionService.getSubscribableCompany(
                pageable, null, authentication);
        // then
        assertThat(subscribableCompany1).hasSize(2)
                .extracting("companyId", "companyImageUrl", "isSubscribed")
                .containsExactly(
                        Tuple.tuple(company5.getId(), company5.getOfficialImageUrl().getUrl(), false),
                        Tuple.tuple(company4.getId(), company4.getOfficialImageUrl().getUrl(), false)
                );

        // when
        Slice<SubscriableCompanyResponse> subscribableCompany2 = memberSubscriptionService.getSubscribableCompany(
                pageable, company4.getId(), authentication);
        // then
        assertThat(subscribableCompany2).hasSize(2)
                .extracting("companyId", "companyImageUrl", "isSubscribed")
                .containsExactly(
                        Tuple.tuple(company3.getId(), company3.getOfficialImageUrl().getUrl(), false),
                        Tuple.tuple(company2.getId(), company2.getOfficialImageUrl().getUrl(), true)
                );

        // when
        Slice<SubscriableCompanyResponse> subscribableCompany3 = memberSubscriptionService.getSubscribableCompany(
                pageable, company2.getId(), authentication);

        // then
        assertThat(subscribableCompany3).hasSize(1)
                .extracting("companyId", "companyImageUrl", "isSubscribed")
                .containsExactly(
                        Tuple.tuple(company1.getId(), company1.getOfficialImageUrl().getUrl(), true)
                );
    }

    @Test
    @DisplayName("회원이 구독하지 않은 구독 가능한 기업 상세 정보를 조회한다.")
    void getCompanyDetailNotSubscribe() {
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
        Company company = createCompany("트이다", "교육", "트이다는..", "https://www.teuida.net/iamge.png",
                "https://www.teuida.net/career");
        companyRepository.save(company);

        // 기술 블로그 생성
        TechArticle techArticle1 = createTechArticle(company);
        TechArticle techArticle2 = createTechArticle(company);
        TechArticle techArticle3 = createTechArticle(company);
        techArticleRepository.saveAll(List.of(techArticle1, techArticle2, techArticle3));

        // when
        CompanyDetailResponse companyDetail = memberSubscriptionService.getCompanyDetail(company.getId(),
                authentication);

        // then
        assertAll(
                () -> assertThat(companyDetail.getCompanyId()).isEqualTo(company.getId()),
                () -> assertThat(companyDetail.getCompanyName()).isEqualTo(company.getName().getCompanyName()),
                () -> assertThat(companyDetail.getIndustry()).isEqualTo(company.getIndustry()),
                () -> assertThat(companyDetail.getCompanyDescription()).isEqualTo(company.getDescription()),
                () -> assertThat(companyDetail.getCompanyCareerUrl()).isEqualTo(company.getCareerUrl().getUrl()),
                () -> assertThat(companyDetail.getCompanyOfficialImageUrl()).isEqualTo(
                        company.getOfficialImageUrl().getUrl()),
                () -> assertThat(companyDetail.getTechArticleTotalCount()).isEqualTo(3L),
                () -> assertThat(companyDetail.getIsSubscribed()).isFalse()
        );
    }

    @Test
    @DisplayName("회원이 이미 구독한 구독 가능한 기업 상세 정보를 조회한다.")
    void getCompanyDetail() {
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
        Company company = createCompany("트이다", "교육", "트이다는..", "https://www.teuida.net/iamge.png",
                "https://www.teuida.net/career");
        companyRepository.save(company);

        // 구독 생성
        Subscription subscription = createSubscription(member, company);
        subscriptionRepository.save(subscription);

        // 기술 블로그 생성
        TechArticle techArticle1 = createTechArticle(company);
        TechArticle techArticle2 = createTechArticle(company);
        TechArticle techArticle3 = createTechArticle(company);
        techArticleRepository.saveAll(List.of(techArticle1, techArticle2, techArticle3));

        // when
        CompanyDetailResponse companyDetail = memberSubscriptionService.getCompanyDetail(company.getId(),
                authentication);

        // then
        assertAll(
                () -> assertThat(companyDetail.getCompanyId()).isEqualTo(company.getId()),
                () -> assertThat(companyDetail.getCompanyName()).isEqualTo(company.getName().getCompanyName()),
                () -> assertThat(companyDetail.getIndustry()).isEqualTo(company.getIndustry()),
                () -> assertThat(companyDetail.getCompanyDescription()).isEqualTo(company.getDescription()),
                () -> assertThat(companyDetail.getCompanyCareerUrl()).isEqualTo(company.getCareerUrl().getUrl()),
                () -> assertThat(companyDetail.getCompanyOfficialImageUrl()).isEqualTo(
                        company.getOfficialImageUrl().getUrl()),
                () -> assertThat(companyDetail.getTechArticleTotalCount()).isEqualTo(3L),
                () -> assertThat(companyDetail.getIsSubscribed()).isTrue()
        );
    }

    @Test
    @DisplayName("회원이 구독 가능한 기업 상세 정보를 조회할 때 기업이 존재하지 않으면 예외가 발생한다.")
    void getCompanyDetailNotFoundCompany() {
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
        assertThatThrownBy(() -> memberSubscriptionService.getCompanyDetail(0L, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(CompanyExceptionMessage.NOT_FOUND_COMPANY_MESSAGE);

    }

    private static TechArticle createTechArticle(Company company) {
        return TechArticle.builder()
                .company(company)
                .build();
    }

    private static Company createCompany(String companyName, String industry, String description,
                                         String officialImageUrl, String careerUrl) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .industry(industry)
                .description(description)
                .officialImageUrl(new Url(officialImageUrl))
                .careerUrl(new Url(careerUrl))
                .build();
    }

    private static Company createCompany(String companyName, String officialImageUrl) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .officialImageUrl(new Url(officialImageUrl))
                .build();
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