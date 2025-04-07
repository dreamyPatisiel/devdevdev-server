package com.dreamypatisiel.devdevdev.domain.repository.notification.custom;

import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.dreamypatisiel.devdevdev.domain.entity.QNotification.notification;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPQLQueryFactory query;

    @Override
    public void bulkMarkAllAsReadByMemberId(Long memberId) {
        query.update(notification)
                .set(notification.isRead, true)
                .where(notification.member.id.eq(memberId)
                        .and(notification.isRead.isFalse()))
                .execute();
    }
}
