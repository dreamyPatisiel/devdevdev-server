package com.dreamypatisiel.devdevdev.global.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TimeUtils 테스트")
class TimeUtilsTest {

    // 기준 시간: 2025-10-26 10:00:00
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 10, 26, 10, 0, 0);

    @ParameterizedTest
    @MethodSource("provideWithinOneWeekDates")
    @DisplayName("날짜 기준 7일 이내는 true를 반환한다")
    void isWithinOneWeek_true(LocalDateTime testTime) {
        // when // then
        assertThat(TimeUtils.isWithinOneWeek(testTime, BASE_TIME))
                .isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideOverOneWeekDates")
    @DisplayName("날짜 기준 7일 초과는 false를 반환한다")
    void isWithinOneWeek_false(LocalDateTime testTime) {
        // when // then
        assertThat(TimeUtils.isWithinOneWeek(testTime, BASE_TIME))
                .isFalse();
    }

    private static Stream<LocalDateTime> provideWithinOneWeekDates() {
        return Stream.of(
                LocalDateTime.of(2025, 10, 26, 10, 0, 0), // 오늘 (2025-10-26)
                LocalDateTime.of(2025, 10, 25, 15, 30, 0), // 1일 전 (2025-10-25)
                LocalDateTime.of(2025, 10, 20, 8, 0, 0),   // 6일 전 (2025-10-20)
                LocalDateTime.of(2025, 10, 19, 23, 59, 59) // 7일 전 (2025-10-19)
        );
    }

    private static Stream<LocalDateTime> provideOverOneWeekDates() {
        return Stream.of(
                LocalDateTime.of(2025, 10, 18, 10, 0, 0), // 8일 전 (2025-10-18)
                LocalDateTime.of(2025, 10, 12, 10, 0, 0), // 14일 전 (2025-10-12)
                LocalDateTime.of(2025, 9, 26, 10, 0, 0)   // 30일 전 (2025-09-26)
        );
    }
}
