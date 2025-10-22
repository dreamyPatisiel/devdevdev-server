package com.dreamypatisiel.devdevdev.global.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TimeUtils 테스트")
class TimeUtilsTest {

    @ParameterizedTest
    @CsvSource({
            "6, DAYS", // 6일 전
            "1, HOURS", // 1시간 전
            "1, MINUTES", // 1분 전
            "6, DAYS", // 6일 23시간 전
    })
    @DisplayName("일주일 이내의 시간은 true를 반환한다")
    void isWithinOneWeek_true(int amount, String unit) {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime testTime = createTestTime(now, amount, unit);

        // when // then
        assertThat(TimeUtils.isWithinOneWeek(testTime))
                .isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "1, WEEKS", // 일주일 전
            "8, DAYS", // 8일 전
            "2, WEEKS", // 2주일 전
            "1, MONTHS" // 1개월 전
    })
    @DisplayName("일주일을 초과한 시간은 false를 반환한다")
    void isWithinOneWeek_false(int amount, String unit) {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime testTime = createTestTime(now, amount, unit);

        // when // then
        assertThat(TimeUtils.isWithinOneWeek(testTime))
                .isFalse();
    }

    private LocalDateTime createTestTime(LocalDateTime baseTime, int amount, String unit) {
        return switch (unit) {
            case "DAYS" -> baseTime.minusDays(amount);
            case "HOURS" -> baseTime.minusHours(amount);
            case "MINUTES" -> baseTime.minusMinutes(amount);
            case "WEEKS" -> baseTime.minusWeeks(amount);
            case "MONTHS" -> baseTime.minusMonths(amount);
            default -> baseTime;
        };
    }
}
