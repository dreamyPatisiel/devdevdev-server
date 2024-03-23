package com.dreamypatisiel.devdevdev.domain.entity;

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
    private String imageUrl;
    private String imageKey;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_option_id")
    private PickOption pickOption;

    @Builder
    private PickOptionImage(String imageUrl, String imageKey, String name) {
        this.imageUrl = imageUrl;
        this.imageKey = imageKey;
        this.name = name;
    }

    public static PickOptionImage create(String imageUrl, String key, String name) {
        PickOptionImage pickOptionImage = new PickOptionImage();
        pickOptionImage.imageUrl = imageUrl;
        pickOptionImage.imageKey = key;
        pickOptionImage.name = name;

        return pickOptionImage;
    }

    public void changePickOptionImage(PickOption pickOption) {
        this.pickOption = pickOption;
    }
}
