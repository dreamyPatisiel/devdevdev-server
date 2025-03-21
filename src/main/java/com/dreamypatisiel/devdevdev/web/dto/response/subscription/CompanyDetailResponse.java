package com.dreamypatisiel.devdevdev.web.dto.response.subscription;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom.dto.CompanyDetailDto;
import lombok.Builder;
import lombok.Data;

@Data
public class CompanyDetailResponse {
    private final Long companyId;
    private final String companyName;
    private final String industry;
    private final String companyDescription;
    private final String companyOfficialImageUrl;
    private final String companyCareerUrl;
    private final Long techArticleTotalCount;
    private final Boolean isSubscribed;

    @Builder
    public CompanyDetailResponse(Long companyId, String companyName, String industry,
                                 String companyDescription, String companyOfficialImageUrl,
                                 String companyCareerUrl, Long techArticleTotalCount, Boolean isSubscribed) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.industry = industry;
        this.companyDescription = companyDescription;
        this.companyOfficialImageUrl = companyOfficialImageUrl;
        this.companyCareerUrl = companyCareerUrl;
        this.techArticleTotalCount = techArticleTotalCount;
        this.isSubscribed = isSubscribed;
    }

    public static CompanyDetailResponse createMemberCompanyDetailResponse(CompanyDetailDto companyDetailDto,
                                                                          Long techArticleTotalCount) {

        boolean isSubscribed = companyDetailDto.getSubscriptionId() != null;

        return CompanyDetailResponse.builder()
                .companyId(companyDetailDto.getCompanyId())
                .companyName(companyDetailDto.getName())
                .industry(companyDetailDto.getIndustry())
                .companyDescription(companyDetailDto.getDescription())
                .companyOfficialImageUrl(companyDetailDto.getOfficialImageUrl())
                .companyCareerUrl(companyDetailDto.getCareerUrl())
                .techArticleTotalCount(techArticleTotalCount)
                .isSubscribed(isSubscribed)
                .build();
    }

    public static CompanyDetailResponse createGuestCompanyDetailResponse(Company company, Long techArticleTotalCount) {
        return CompanyDetailResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName().getCompanyName())
                .industry(company.getIndustry())
                .companyDescription(company.getDescription())
                .companyOfficialImageUrl(company.getOfficialImageUrl().getUrl())
                .companyCareerUrl(company.getCareerUrl().getUrl())
                .techArticleTotalCount(techArticleTotalCount)
                .isSubscribed(false)
                .build();
    }
}
