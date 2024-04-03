package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PickVote extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_option_id")
    private PickOption pickOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_id")
    private Pick pick;

    @Builder
    private PickVote(Member member, PickOption pickOption, Pick pick) {
        this.member = member;
        this.pickOption = pickOption;
        this.pick = pick;
    }

    public static PickVote create(Member member, PickOption pickOption) {
        PickVote pickVote = new PickVote();
        pickVote.member = member;
        pickVote.pickOption = pickOption;

        return pickVote;
    }

    public void changePick(Pick pick) {
        this.pick = pick;
        pick.getPickVotes().add(this);
    }

    public void changePickOption(PickOption pickOption) {
        this.pickOption = pickOption;
        pickOption.getPickVotes().add(this);
    }
}
