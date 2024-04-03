package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    @AttributeOverride(name = "companyName",
            column = @Column(name = "name")
    )
    private CompanyName name;

    @Embedded
    @AttributeOverride(name = "url",
            column = @Column(name = "thumbnailUrl")
    )
    private Url thumbnailUrl;
    @Embedded
    @AttributeOverride(name = "url",
            column = @Column(name = "careerUrl")
    )
    private Url careerUrl;

    @OneToMany(mappedBy = "company")
    private List<TechArticle> techArticles = new ArrayList<>();

    @Builder
    private Company(CompanyName name, Url thumbnailUrl, Url careerUrl, List<TechArticle> techArticles) {
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
        this.careerUrl = careerUrl;
        this.techArticles = techArticles;
    }

    public static Company of(CompanyName name, Url thumbnailUrl, Url careerUrl) {
        return Company.builder()
                .name(name)
                .thumbnailUrl(thumbnailUrl)
                .careerUrl(careerUrl)
                .build();
    }

    public void changeTechArticles(List<TechArticle> techArticles) {
        for(TechArticle techArticle : techArticles) {
            techArticle.changeCompany(this);
            this.getTechArticles().add(techArticle);
        }
    }
}