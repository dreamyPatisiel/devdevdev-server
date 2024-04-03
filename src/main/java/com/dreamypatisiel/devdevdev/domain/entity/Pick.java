package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.policy.PopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pick extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private Pick(Title title, Count voteTotalCount, Count viewTotalCount, Count commentTotalCount, Count popularScore, String thumbnailUrl,
                String author, Member member) {
        this.title = title;
        this.voteTotalCount = voteTotalCount;
        this.viewTotalCount = viewTotalCount;
        this.commentTotalCount = commentTotalCount;
        this.popularScore = popularScore;
        this.thumbnailUrl = thumbnailUrl;
        this.author = author;
        this.member = member;
    }

    public static Pick create(Title title, String author, Member member) {
        Pick pick = new Pick();
        pick.title = title;
        pick.voteTotalCount = new Count(0);
        pick.viewTotalCount = new Count(0);
        pick.commentTotalCount = new Count(0);
        pick.popularScore = new Count(0);
        pick.member = member;
        pick.author = author;

        return pick;
    }

    // 연관관계 편의 메소드
    public void changePickOptions(List<PickOption> pickOptions) {
        for(PickOption pickOption : pickOptions) {
            pickOption.changePick(this);
            this.getPickOptions().add(pickOption);
        }
    }

    public void changePickVote(List<PickVote> pickVotes) {
        for(PickVote pickVote : pickVotes) {
            pickVote.changePick(this);
            this.getPickVotes().add(pickVote);
        }
    }

    public boolean isEqualsPick(Pick pick) {
        return this.equals(pick);
    }

    public void changePopularScore(PickPopularScorePolicy policy) {
        this.popularScore = this.calculatePopularScore(policy);
    }

    private Count calculatePopularScore(PickPopularScorePolicy policy) {
        return policy.calculatePopularScore(this);
    }
}
