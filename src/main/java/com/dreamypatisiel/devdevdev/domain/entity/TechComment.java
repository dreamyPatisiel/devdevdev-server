package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx__comment__tech_article__member__deleted_at", columnList = "id, tech_article_id, member_id, deletedAt")
})
public class TechComment extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "commentContents",
            column = @Column(name = "contents", length = 1000)
    )
    private CommentContents contents;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "blame_total_count")
    )
    private Count blameTotalCount;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "recommend_total_count")
    )
    private Count recommendTotalCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_article_id", nullable = false)
    private TechArticle techArticle;

    private LocalDateTime deletedAt;

    @Builder
    private TechComment(Long id, CommentContents contents, Count blameTotalCount, Count recommendTotalCount,
                        Member member, TechArticle techArticle) {
        this.id = id;
        this.contents = contents;
        this.blameTotalCount = blameTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.member = member;
        this.techArticle = techArticle;
    }

    public static TechComment create(CommentContents contents, Member member, TechArticle techArticle) {
        return TechComment.builder()
                .contents(contents)
                .member(member)
                .techArticle(techArticle)
                .blameTotalCount(Count.defaultCount())
                .recommendTotalCount(Count.defaultCount())
                .build();
    }

    public void changeDeletedAt(LocalDateTime now) {
        this.deletedAt = now;
    }

    public void changeCommentContents(CommentContents contents) {
        this.contents = contents;
    }
}
