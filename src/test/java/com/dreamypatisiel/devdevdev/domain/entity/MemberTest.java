package com.dreamypatisiel.devdevdev.domain.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MemberTest {

    @ParameterizedTest
    @CsvSource({
            ", true",           // 변경 이력 없음(null)
            "60, false",         // 24시간 이내
            "1439, false",        // 24시간 이내
            "1440, true",        // 24시간 경과(경계)
            "1550, true",        // 24시간 초과
    })
    @DisplayName("닉네임 변경 가능 여부 파라미터 테스트")
    void canChangeNickname(Long minutesAgo, boolean expected) {
        // given
        LocalDateTime now = LocalDateTime.now();
        Member member = new Member();
        if (minutesAgo != null) {
            member.changeNickname("닉네임", now.minusMinutes(minutesAgo));
        }
        int restrictionMinutes = 1440; // 24시간
        // when
        boolean result = member.canChangeNickname(restrictionMinutes, now);
        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            ", true",           // 변경 이력 없음(null)
            "0, false",         // 24시간 이내
            "1, true",        // 24시간 이내
            "60, true",        // 24시간 경과(경계)
            "1440, true",        // 24시간 초과
    })
    @DisplayName("닉네임 변경 가능 여부 파라미터 테스트")
    void canChangeNicknameWhenDev(Long minutesAgo, boolean expected) {
        // given
        LocalDateTime now = LocalDateTime.now();
        Member member = new Member();
        if (minutesAgo != null) {
            member.changeNickname("닉네임", now.minusMinutes(minutesAgo));
        }
        int restrictionMinutes = 1; // 1분
        // when
        boolean result = member.canChangeNickname(restrictionMinutes, now);
        // then
        assertThat(result).isEqualTo(expected);
    }
}
