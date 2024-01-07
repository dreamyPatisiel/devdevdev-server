package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.ExperienceException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Experience {
    public static final int MIN_EXPERIENCE = 0;
    public static final int MAX_EXPERIENCE = 50;
    public static final String INVALID_EXPERIENCE_MESSAGE = "연차는 %d년에서 %d년 사이의 값이어야 합니다.";

    private Integer experience;

    public Experience(Integer experience) {
        validationExperience(experience);
        this.experience = experience;
    }

    private void validationExperience(Integer experience) {
        if(!isExperienceRange(experience)) {
            throw new ExperienceException(getInvalidRangeExceptionMessage());
        }
    }

    private boolean isExperienceRange(Integer experience) {
        return experience >= MIN_EXPERIENCE && experience <= MAX_EXPERIENCE;
    }

    public static String getInvalidRangeExceptionMessage() {
        return String.format(INVALID_EXPERIENCE_MESSAGE, MIN_EXPERIENCE, MAX_EXPERIENCE);
    }
}
