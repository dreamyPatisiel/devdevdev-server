package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.SubscriptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription.GuestSubscriptionService;
import com.dreamypatisiel.devdevdev.web.dto.response.subscription.CompanyDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.subscription.SubscriableCompanyResponse;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class GuestSubscriptionServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    GuestSubscriptionService guestSubscriptionService;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    SubscriptionRepository subscriptionRepository;
    @Autowired
    private TechArticleRepository techArticleRepository;

    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        // given
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("구독 가능한 기업 목록을 커서 방식으로 조회한다.")
    void getSubscribableCompany() {
        // given
        // 회사 생성
        Company company1 = createCompany("teuida1", "https://www.teuida1.net");
        Company company2 = createCompany("teuida2", "https://www.teuida2.net");
        Company company3 = createCompany("teuida3", "https://www.teuida3.net");
        Company company4 = createCompany("teuida4", "https://www.teuida4.net");
        Company company5 = createCompany("teuida5", "https://www.teuida5.net");
        companyRepository.saveAll(List.of(company1, company2, company3, company4, company5));

        Pageable pageable = PageRequest.of(0, 2);

        // when
        Slice<SubscriableCompanyResponse> subscribableCompany1 = guestSubscriptionService.getSubscribableCompany(
                pageable, null, authentication);
        // then
        assertThat(subscribableCompany1).hasSize(2)
                .extracting("companyId", "companyImageUrl", "isSubscribed")
                .containsExactly(
                        Tuple.tuple(company5.getId(), company5.getOfficialImageUrl().getUrl(), false),
                        Tuple.tuple(company4.getId(), company4.getOfficialImageUrl().getUrl(), false)
                );

        // when
        Slice<SubscriableCompanyResponse> subscribableCompany2 = guestSubscriptionService.getSubscribableCompany(
                pageable, company4.getId(), authentication);
        // then
        assertThat(subscribableCompany2).hasSize(2)
                .extracting("companyId", "companyImageUrl", "isSubscribed")
                .containsExactly(
                        Tuple.tuple(company3.getId(), company3.getOfficialImageUrl().getUrl(), false),
                        Tuple.tuple(company2.getId(), company2.getOfficialImageUrl().getUrl(), false)
                );

        // when
        Slice<SubscriableCompanyResponse> subscribableCompany3 = guestSubscriptionService.getSubscribableCompany(
                pageable, company2.getId(), authentication);

        // then
        assertThat(subscribableCompany3).hasSize(1)
                .extracting("companyId", "companyImageUrl", "isSubscribed")
                .containsExactly(
                        Tuple.tuple(company1.getId(), company1.getOfficialImageUrl().getUrl(), false)
                );
    }

    @Test
    @DisplayName("회원이 구독하지 않은 구독 가능한 기업 상세 정보를 조회한다.")
    void getCompanyDetailNotSubscribe() {
        // given
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
        CompanyDetailResponse companyDetail = guestSubscriptionService.getCompanyDetail(company.getId(),
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

    private static TechArticle createTechArticle(Company company) {
        return TechArticle.builder()
                .company(company)
                .build();
    }

    private static Company createCompany(String companyName, String officialImageUrl) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .officialImageUrl(new Url(officialImageUrl))
                .build();
    }
}