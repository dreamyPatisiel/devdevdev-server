package com.dreamypatisiel.devdevdev.domain.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    SUBSCRIPTION, // 구독 알림
    COMMENT_AND_REPLY; // 댓글, 대댓글
  
    // 현재 서비스 제공 중인 알림 타입 리스트
    private static final List<NotificationType> ENABLED_TYPES = List.of(SUBSCRIPTION);

    public static List<NotificationType> getEnabledTypes() {
        return ENABLED_TYPES;
    }
}
