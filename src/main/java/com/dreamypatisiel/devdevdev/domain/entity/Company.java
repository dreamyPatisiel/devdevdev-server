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

    @Embedded
    @AttributeOverride(name = "url",
            column = @Column(name = "official_image_url")
    )
    private Url officialImageUrl;

    @Embedded
    @AttributeOverride(name = "url",
            column = @Column(name = "career_url")
    )
    private Url careerUrl;

    @Column(length = 10)
    private String industry;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "company")
    private List<TechArticle> techArticles = new ArrayList<>();

    @Builder
    private Company(CompanyName name, Url officialUrl, Url officialImageUrl, Url careerUrl, String industry,
                    String description) {
        this.name = name;
        this.officialUrl = officialUrl;
        this.officialImageUrl = officialImageUrl;
        this.careerUrl = careerUrl;
        this.industry = industry;
        this.description = description;
    }

    public boolean isEqualsId(Long id) {
        return this.id.equals(id);
    }
}