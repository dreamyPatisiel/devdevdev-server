package com.dreamypatisiel.devdevdev.global.utils;

import java.time.LocalDateTime;

/**
 * 시간 관련 유틸리티 클래스
 */
public abstract class TimeUtils {

    /**
     * 주어진 시간이 현재로부터 일주일 이내인지 확인합니다.
     * 
     * @param createdAt 확인할 시간
     * @return 일주일 이내라면 true, 그렇지 않다면 false
     */
    public static boolean isWithinOneWeek(LocalDateTime createdAt) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        return createdAt.isAfter(oneWeekAgo);
    }
}
