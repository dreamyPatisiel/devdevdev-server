package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
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

    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anonymous_member_id")
    private AnonymousMember anonymousMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_option_id", nullable = false)
    private PickOption pickOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_id", nullable = false)
    private Pick pick;

    @Builder
    private PickVote(Member member, AnonymousMember anonymousMember, PickOption pickOption, Pick pick) {
        this.member = member;
        this.anonymousMember = anonymousMember;
        this.pickOption = pickOption;
        this.pick = pick;
    }

    public static PickVote createByMember(Member member, Pick pick, PickOption pickOption) {
        PickVote pickVote = new PickVote();
        pickVote.member = member;
        pickVote.changePick(pick);
        pickVote.changePickOption(pickOption);

        return pickVote;
    }

    public static PickVote createByAnonymous(AnonymousMember anonymousMember, Pick pick, PickOption pickOption) {
        PickVote pickVote = new PickVote();
        pickVote.anonymousMember = anonymousMember;
        pickVote.changePick(pick);
        pickVote.changePickOption(pickOption);

        return pickVote;
    }

    // 연관관계 편의 메소드
    public void changePick(Pick pick) {
        this.pick = pick;
        pick.getPickVotes().add(this);
    }

    // 연관관계 편의 메소드
    public void changePickOption(PickOption pickOption) {
        this.pickOption = pickOption;
        pickOption.getPickVotes().add(this);
    }

    public boolean isAnonymousMemberNotNull() {
        return this.anonymousMember != null;
    }

    public boolean isMemberNotNull() {
        return this.member != null;
    }

    public boolean isEqualsMemberId(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    public void delete(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
