package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyVersion extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false)
    private String versionName;
    
    private Boolean isActive;

    @Builder
    private SurveyVersion(String versionName, Boolean isActive) {
        this.versionName = versionName;
        this.isActive = isActive;
    }

    public void changeIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
