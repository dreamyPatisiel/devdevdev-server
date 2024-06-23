package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PickOptionImage extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String imageUrl;

    @Column(length = 500, nullable = false)
    private String imageKey;

    @Column(length = 255, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_option_id", nullable = false)
    private PickOption pickOption;

    @Builder
    private PickOptionImage(String imageUrl, String imageKey, String name, PickOption pickOption) {
        this.imageUrl = imageUrl;
        this.imageKey = imageKey;
        this.name = name;
        this.pickOption = pickOption;
    }

    public static PickOptionImage create(String imageUrl, String key, String name) {
        PickOptionImage pickOptionImage = new PickOptionImage();
        pickOptionImage.imageUrl = imageUrl;
        pickOptionImage.imageKey = key;
        pickOptionImage.name = name;

        return pickOptionImage;
    }

    public void changePickOption(PickOption pickOption) {
        pickOption.getPickOptionImages().add(this);
        this.pickOption = pickOption;
    }
}
