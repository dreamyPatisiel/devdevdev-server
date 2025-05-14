package com.dreamypatisiel.devdevdev.web.dto.response.subscription;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import lombok.Builder;
import lombok.Data;

@Data
public class SubscribedCompanyResponse {
    private final Long companyId;
    private final String companyName;
    private final String companyImageUrl;
    private final Boolean isSubscribed;

    @Builder
    public SubscribedCompanyResponse(Long companyId, String companyName, String companyImageUrl, Boolean isSubscribed) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyImageUrl = companyImageUrl;
        this.isSubscribed = isSubscribed;
    }

    public static SubscribedCompanyResponse create(Company company) {
        return SubscribedCompanyResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName().getCompanyName())
                .companyImageUrl(company.getOfficialImageUrl().getUrl())
                .isSubscribed(false)
                .build();
    }

    public static SubscribedCompanyResponse createWithIsSubscribed(Company company, Boolean isSubscribed) {
        return SubscribedCompanyResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName().getCompanyName())
                .companyImageUrl(company.getOfficialImageUrl().getUrl())
                .isSubscribed(isSubscribed)
                .build();
    }
}
