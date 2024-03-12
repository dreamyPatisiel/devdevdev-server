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
    private String key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_option_id")
    private PickOption pickOption;

    @Builder
    private PickOptionImage(String imageUrl, PickOption pickOption) {
        this.imageUrl = imageUrl;
        this.pickOption = pickOption;
    }

    public static PickOptionImage create(String imageUrl, String key) {
        PickOptionImage pickOptionImage = new PickOptionImage();
        pickOptionImage.imageUrl = imageUrl;
        pickOptionImage.key = key;

        return pickOptionImage;
    }

    public void changePickOptionImage(PickOption pickOption) {
        this.pickOption = pickOption;
    }
}
