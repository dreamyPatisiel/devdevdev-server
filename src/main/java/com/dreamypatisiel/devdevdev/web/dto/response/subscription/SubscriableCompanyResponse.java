package com.dreamypatisiel.devdevdev.web.dto.response.subscription;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import lombok.Builder;
import lombok.Data;

@Data
public class SubscriableCompanyResponse {
    private final Long companyId;
    private final String companyName;
    private final String companyImageUrl;
    private final Boolean isSubscribed;

    @Builder
    public SubscriableCompanyResponse(Long companyId, String companyName, String companyImageUrl, Boolean isSubscribed) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyImageUrl = companyImageUrl;
        this.isSubscribed = isSubscribed;
    }

    public static SubscriableCompanyResponse create(Company company) {
        return SubscriableCompanyResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName().getCompanyName())
                .companyImageUrl(company.getOfficialImageUrl().getUrl())
                .isSubscribed(false)
                .build();
    }

    public static SubscriableCompanyResponse createWithIsSubscribed(Company company, Boolean isSubscribed) {
        return SubscriableCompanyResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName().getCompanyName())
                .companyImageUrl(company.getOfficialImageUrl().getUrl())
                .isSubscribed(isSubscribed)
                .build();
    }
}
