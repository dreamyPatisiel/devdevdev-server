package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.SubscriptionRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.SubscriableCompanyResponse;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription.GuestSubscriptionService;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticKeywordRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    ElasticKeywordRepository elasticKeywordRepository;
    @MockBean
    ElasticTechArticleRepository elasticTechArticleRepository;

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

    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;

    @Test
    @DisplayName("구독 가능한 기업 목록을 커서 방식으로 조회한다.")
    void getSubscribableCompany() {
        // given
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

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

    private static Company createCompany(String companyName, String officialImageUrl) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .officialImageUrl(new Url(officialImageUrl))
                .build();
    }
}