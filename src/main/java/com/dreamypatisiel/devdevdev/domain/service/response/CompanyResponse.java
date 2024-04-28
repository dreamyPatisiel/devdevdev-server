package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CompanyResponse {

    public Long id;
    public String name;
    public String careerUrl;

    @Builder
    private CompanyResponse(Long id, String name, String careerUrl) {
        this.id = id;
        this.name = name;
        this.careerUrl = careerUrl;
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
                .build();
    }
}
