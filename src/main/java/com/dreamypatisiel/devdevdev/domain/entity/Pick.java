package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
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
    private Pick(Title title, Count voteTotalCount, Count viewTotalCount, Count commentTotalCount, String thumbnailUrl,
                String author, Member member) {
        this.title = title;
        this.voteTotalCount = voteTotalCount;
        this.viewTotalCount = viewTotalCount;
        this.commentTotalCount = commentTotalCount;
        this.thumbnailUrl = thumbnailUrl;
        this.author = author;
        this.member = member;
    }

    public static Pick create(Title title, Count voteTotalCount, Count viewTotalCount, Count commentTotalCount, String thumbnailUrl,
                              String author, List<PickOption> pickOptions, List<PickVote> pickVotes) {
        Pick pick = new Pick();
        pick.title = title;
        pick.voteTotalCount = voteTotalCount;
        pick.viewTotalCount = viewTotalCount;
        pick.commentTotalCount = commentTotalCount;
        pick.author = author;
        pick.thumbnailUrl = thumbnailUrl;
        pick.changePickOptions(pickOptions);
        pick.changePickVote(pickVotes);

        return pick;
    }

    // 연관관계 편의 메소드
    private void changePickOptions(List<PickOption> pickOptions) {
        for(PickOption pickOption : pickOptions) {
            pickOption.changePick(this);
            this.getPickOptions().add(pickOption);
        }
    }

    private void changePickVote(List<PickVote> pickVotes) {
        for(PickVote pickVote : pickVotes) {
            pickVote.changePick(this);
            this.getPickVotes().add(pickVote);
        }
    }

    public boolean isEqualsPick(Pick pick) {
        return this.equals(pick);
    }
}
