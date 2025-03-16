package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class CompanyDetailDto {
    private Long companyId;
    private String careerUrl;
    private String name;
    private String industry;
    private String officialImageUrl;
    private String description;
    private Long subscriptionId;

    @QueryProjection
    public CompanyDetailDto(Long companyId, String careerUrl, String name, String industry, String officialImageUrl,
                            String description, Long subscriptionId) {
        this.companyId = companyId;
        this.careerUrl = careerUrl;
        this.name = name;
        this.industry = industry;
        this.officialImageUrl = officialImageUrl;
        this.description = description;
        this.subscriptionId = subscriptionId;
    }
}
