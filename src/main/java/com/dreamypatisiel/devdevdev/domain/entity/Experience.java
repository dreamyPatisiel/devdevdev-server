package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.exception.ExperienceException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Experience {
    private static final int MIN_EXPERIENCE = 0;
    private static final int MAX_EXPERIENCE = 50;
    public static final String INVALID_EXPERIENCE_MESSAGE = "연차는 0년에서 50년 사이의 값이어야 합니다.";
    private Integer experience;

    public Experience(Integer experience) {
        validationExperience(experience);
        this.experience = experience;
    }

    private void validationExperience(Integer experience) {
        if(!isExperienceRange(experience)) {
            throw new ExperienceException(INVALID_EXPERIENCE_MESSAGE);
        }
    }

    private boolean isExperienceRange(Integer experience) {
        return experience >= MIN_EXPERIENCE && experience <= MAX_EXPERIENCE;
    }
}
