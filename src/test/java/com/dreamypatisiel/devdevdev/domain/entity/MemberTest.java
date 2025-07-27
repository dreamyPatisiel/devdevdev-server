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
            "0, false",         // 24시간 이내
            "1, false",        // 24시간 이내
            "24, true",        // 24시간 경과(경계)
            "25, true",        // 24시간 초과
    })
    @DisplayName("닉네임 변경 가능 여부 파라미터 테스트")
    void canChangeNickname(Long hoursAgo, boolean expected) {
        // given
        LocalDateTime now = LocalDateTime.now();
        Member member = new Member();
        if (hoursAgo != null) {
            member.changeNickname("닉네임", now.minusHours(hoursAgo));
        }
        int nicknameChangeIntervalHours = 24;
        // when
        boolean result = member.canChangeNickname(nicknameChangeIntervalHours, now);
        // then
        assertThat(result).isEqualTo(expected);
    }
}
