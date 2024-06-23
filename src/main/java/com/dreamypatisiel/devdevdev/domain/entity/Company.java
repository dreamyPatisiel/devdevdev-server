package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "companyName",
            column = @Column(name = "name", length = 30, nullable = false)
    )
    private CompanyName name;

    @Embedded
    @AttributeOverride(name = "url",
            column = @Column(name = "official_url")
    )
    private Url officialUrl;

    private String officialImageUrl;

    @Embedded
    @AttributeOverride(name = "url",
            column = @Column(name = "career_url")
    )
    private Url careerUrl;

    @OneToMany(mappedBy = "company")
    private List<TechArticle> techArticles = new ArrayList<>();

    @Builder
    private Company(CompanyName name, Url officialUrl, String officialImageUrl, Url careerUrl,
                    List<TechArticle> techArticles) {
        this.name = name;
        this.officialUrl = officialUrl;
        this.officialImageUrl = officialImageUrl;
        this.careerUrl = careerUrl;
        this.techArticles = techArticles;
    }

    public void changeTechArticles(List<TechArticle> techArticles) {
        for (TechArticle techArticle : techArticles) {
            techArticle.changeCompany(this);
            this.getTechArticles().add(techArticle);
        }
    }
}