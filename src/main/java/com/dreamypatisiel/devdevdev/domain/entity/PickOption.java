package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.global.utils.BigDecimalUtils;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickOptionRequest;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PickOption extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_id", nullable = false)
    private Pick pick;

    @Embedded
    @Column(length = 150, nullable = false)
    private Title title;

    @Embedded
    @AttributeOverride(name = "pickContents",
            column = @Column(name = "contents", columnDefinition = "mediumtext")
    )
    private PickOptionContents contents;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "vote_total_count")
    )
    private Count voteTotalCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PickOptionType pickOptionType;

    @OneToMany(mappedBy = "pickOption")
    private List<PickVote> pickVotes = new ArrayList<>();

    @OneToMany(mappedBy = "pickOption")
    private List<PickOptionImage> pickOptionImages = new ArrayList<>();

    @Builder
    private PickOption(Title title, PickOptionContents contents, Count voteTotalCount, PickOptionType pickOptionType,
                       Pick pick) {
        this.title = title;
        this.contents = contents;
        this.voteTotalCount = voteTotalCount;
        this.pickOptionType = pickOptionType;
        this.pick = pick;
    }

    public static PickOption create(Title title, PickOptionContents pickOptionContents, PickOptionType pickOptionType,
                                    List<PickOptionImage> pickOptionImages, Pick pick) {
        PickOption pickOption = new PickOption();
        pickOption.title = title;
        pickOption.contents = pickOptionContents;
        pickOption.voteTotalCount = new Count(0);
        pickOption.pickOptionType = pickOptionType;
        pickOption.changePickOptionImages(pickOptionImages);
        pickOption.changePick(pick);

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
        pick.getPickOptions().add(this);
        this.pick = pick;
    }

    // 연관관계 편의 메소드
    public void changePickOptionImages(List<PickOptionImage> pickOptionImages) {
        for (PickOptionImage pickOptionImage : pickOptionImages) {
            pickOptionImage.changePickOption(this);
            this.getPickOptionImages().add(pickOptionImage);
        }
    }

    public void changePickVoteCount(Count voteTotalCount) {
        this.voteTotalCount = voteTotalCount;
    }

    public void changePickOption(ModifyPickOptionRequest modifyPickOptionRequest) {
        this.title = new Title(modifyPickOptionRequest.getPickOptionTitle());
        this.contents = new PickOptionContents(modifyPickOptionRequest.getPickOptionContent());
    }

    public boolean isEqualsId(Long id) {
        return this.id.equals(id);
    }

    public boolean isEqualsPickOption(PickOption pickOption) {
        return this.equals(pickOption);
    }

    public void plusOneVoteTotalCount() {
        this.voteTotalCount = Count.plusOne(this.voteTotalCount);
    }

    public void minusVoteTotalCount() {
        this.voteTotalCount = Count.minusOne(this.voteTotalCount);
    }

    public String getContentsAsString() {
        if (this.contents == null) {
            return null;
        }

        return contents.getPickOptionContents();
    }
}