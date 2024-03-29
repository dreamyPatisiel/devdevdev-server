package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.global.utils.BigDecimalUtils;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PickOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_id")
    private Pick pick;

    @Embedded
    @Column(length = 150)
    private Title title;

    @Embedded
    @AttributeOverride(name = "pickContents",
            column = @Column(name = "contents")
    )
    private PickOptionContents contents;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "vote_total_count")
    )
    private Count voteTotalCount;

    @OneToMany(mappedBy = "pickOption")
    private List<PickVote> pickVotes = new ArrayList<>();

    @OneToMany(mappedBy = "pickOption")
    private List<PickOptionImage> pickOptionImages = new ArrayList<>();

    @Builder
    private PickOption(Title title, PickOptionContents contents, Count voteTotalCount) {
        this.title = title;
        this.contents = contents;
        this.voteTotalCount = voteTotalCount;
    }

    public static PickOption create(Title title, PickOptionContents pickOptionContents) {
        PickOption pickOption = new PickOption();
        pickOption.title = title;
        pickOption.contents = pickOptionContents;
        pickOption.voteTotalCount = new Count(0);

        return pickOption;
    }

    public static BigDecimal calculatePercentBy(Pick pick, PickOption pickOption) {
        Count pickOptionVoteTotalCount = pickOption.getVoteTotalCount();
        Count pickVoteTotalCount = pick.getVoteTotalCount();
        return BigDecimalUtils.toPercentageOf(
                BigDecimal.valueOf(pickOptionVoteTotalCount.getCount()),
                BigDecimal.valueOf(pickVoteTotalCount.getCount())
        );
    }

    public void changePick(Pick pick) {
        this.pick = pick;
    }

    // 연관관계 편의 메소드
    public void changePickOptionImages(List<PickOptionImage> pickOptionImages) {
        for(PickOptionImage pickOptionImage : pickOptionImages) {
            pickOptionImage.changePickOptionImage(this);
            this.getPickOptionImages().add(pickOptionImage);
        }
    }

    public void changePickVoteCount(Count voteTotalCount) {
        this.voteTotalCount = voteTotalCount;
    }
}
