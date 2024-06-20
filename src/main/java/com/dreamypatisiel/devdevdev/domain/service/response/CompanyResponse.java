package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import lombok.Builder;
import lombok.Data;

@Data
public class CompanyResponse {

    public final Long id;
    public final String name;
    public final String careerUrl;
    public final String thumbnailImageUrl;

    @Builder
    private CompanyResponse(Long id, String name, String careerUrl, String thumbnailImageUrl) {
        this.id = id;
        this.name = name;
        this.careerUrl = careerUrl;
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    public static CompanyResponse of(Long id, String name, String careerUrl) {
        return CompanyResponse.builder()
                .id(id)
                .name(name)
                .careerUrl(careerUrl)
                .build();
    }

    public static CompanyResponse from(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName().getCompanyName())
                .careerUrl(company.getCareerUrl().getUrl())
                .thumbnailImageUrl(company.getThumbnailImageUrl())
                .build();
    }
}
