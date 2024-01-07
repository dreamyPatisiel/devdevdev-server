package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechArticle extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "view_total_count")
    )
    private Count viewTotalCount;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "recommend_total_count")
    )
    private Count recommendTotalCount;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "comment_total_count")
    )
    private Count commentTotalCount;

    @Embedded
    @AttributeOverride(name = "url",
            column = @Column(name = "techArticleLink")
    )
    private Url techArticleLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
}
