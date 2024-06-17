package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.util.ObjectUtils;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx__content_status", columnList = "contentStatus"),
        @Index(name = "idx__member", columnList = "member")
})
public class Pick extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @Column(length = 150)
    private Title title;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "vote_total_count")
    )
    private Count voteTotalCount;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "view_total_count")
    )
    private Count viewTotalCount;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "comment_total_count")
    )
    private Count commentTotalCount;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "popluar_score")
    )
    private Count popularScore;

    private String thumbnailUrl;
    private String author;

    @Enumerated(EnumType.STRING)
    private ContentStatus contentStatus;

    @Column(columnDefinition = "longtext")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> embeddings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "pick")
    private List<PickComment> pickComments = new ArrayList<>();

    @OneToMany(mappedBy = "pick")
    private List<PickReply> pickReplies = new ArrayList<>();

    @OneToMany(mappedBy = "pick")
    private List<PickOption> pickOptions = new ArrayList<>();

    @OneToMany(mappedBy = "pick")
    private List<PickVote> pickVotes = new ArrayList<>();

    @Builder
    private Pick(Title title, Count voteTotalCount, Count viewTotalCount, Count commentTotalCount, Count popularScore,
                 String thumbnailUrl, String author, ContentStatus contentStatus, List<Double> embeddings,
                 Member member) {
        this.title = title;
        this.voteTotalCount = voteTotalCount;
        this.viewTotalCount = viewTotalCount;
        this.commentTotalCount = commentTotalCount;
        this.popularScore = popularScore;
        this.thumbnailUrl = thumbnailUrl;
        this.author = author;
        this.contentStatus = contentStatus;
        this.embeddings = this.convertEmbeddings(embeddings);
        this.member = member;
    }

    public static Pick create(Title title, String author, Member member) {
        Pick pick = new Pick();
        pick.title = title;
        pick.voteTotalCount = new Count(0);
        pick.viewTotalCount = new Count(0);
        pick.commentTotalCount = new Count(0);
        pick.popularScore = new Count(0);
        pick.author = author;
        pick.contentStatus = getDefaultContentStatusByMemberRole(member);
        pick.member = member;

        return pick;
    }

    private static ContentStatus getDefaultContentStatusByMemberRole(Member member) {
        if (member.isAdmin()) {
            return ContentStatus.APPROVAL;
        }
        return ContentStatus.READY;
    }

    // 연관관계 편의 메소드
    public void changePickOptions(List<PickOption> pickOptions) {
        for (PickOption pickOption : pickOptions) {
            pickOption.changePick(this);
            this.getPickOptions().add(pickOption);
        }
    }

    public void changePickVote(List<PickVote> pickVotes) {
        for (PickVote pickVote : pickVotes) {
            pickVote.changePick(this);
            this.getPickVotes().add(pickVote);
        }
    }

    public boolean isEqualsPick(Pick pick) {
        return this.equals(pick);
    }

    public boolean isEqualsId(Long id) {
        return this.id.equals(id);
    }

    public void changePopularScore(PickPopularScorePolicy policy) {
        this.popularScore = this.calculatePopularScore(policy);
    }

    private Count calculatePopularScore(PickPopularScorePolicy policy) {
        return policy.calculatePopularScore(this);
    }

    public boolean isEqualMember(Member member) {
        return this.member.isEqualMember(member);
    }

    public void changeTitle(String title) {
        this.title = new Title(title);
    }

    public void plusOneViewTotalCount() {
        this.viewTotalCount = Count.plusOne(this.viewTotalCount);
    }

    public void plusOneVoteTotalCount() {
        this.voteTotalCount = Count.plusOne(this.voteTotalCount);
    }

    public void minusVoteTotalCount() {
        this.voteTotalCount = Count.minusOne(this.voteTotalCount);
    }

    public void changeContentStatus(ContentStatus contentStatus) {
        this.contentStatus = contentStatus;
    }

    public boolean isTrueContentStatus(ContentStatus contentStatus) {
        return this.contentStatus.equals(contentStatus);
    }

    public void changeEmbeddings(List<Double> embeddings) {
        this.embeddings = convertEmbeddings(embeddings);
    }

    public List<Double> getEmbeddings() {
        return this.embeddings.stream()
                .map(Double::valueOf)
                .toList();
    }

    private List<String> convertEmbeddings(List<Double> embeddings) {

        if (ObjectUtils.isEmpty(embeddings)) {
            return Collections.emptyList();
        }

        return embeddings.stream()
                .map(embedding -> String.valueOf(embedding.doubleValue()))
                .toList();
    }
}
