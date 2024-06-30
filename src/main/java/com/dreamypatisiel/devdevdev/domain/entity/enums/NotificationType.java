package com.dreamypatisiel.devdevdev.domain.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    // 구독 알림
    // 댓글, 대댓글
    SUBSCRIPTION {
        @Override
        public String createMessage() {
            return null;
        }
    }, COMMENT_AND_REPLY {
        @Override
        public String createMessage() {
            return null;
        }
    };

    private String message;
    abstract public String createMessage();
}
