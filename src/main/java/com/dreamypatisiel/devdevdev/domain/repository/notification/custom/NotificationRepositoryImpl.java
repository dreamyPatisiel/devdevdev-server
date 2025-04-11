package com.dreamypatisiel.devdevdev.domain.repository.notification.custom;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

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

    @Override
    public SliceCustom<Notification> findNotificationsByMemberOrderByCreatedAtDesc(Pageable pageable, Member member) {
        List<Notification> contents = query.selectFrom(notification)
                .where(notification.member.eq(member))
                .orderBy(notification.createdAt.desc(), notification.id.desc())
                .limit(pageable.getPageSize())
                .fetch();

        // 회원이 읽지 않은 알림 개수
        long unReadNotificationTotalCount = countByMemberAndIsReadFalse(member);

        return new SliceCustom<>(contents, pageable, false, unReadNotificationTotalCount);    }

    @Override
    public SliceCustom<Notification> findNotificationsByMemberAndCursor(Pageable pageable, Long notificationId, Member findMember) {
        List<Notification> contents = query.selectFrom(notification)
                .where(notification.member.eq(findMember)
                        .and(getCursorCondition(notificationId)))
                .orderBy(notification.createdAt.desc(), notification.id.desc())
                .limit(pageable.getPageSize())
                .fetch();

        // 회원이 읽지 않은 알림 개수
        long unReadNotificationTotalCount = countByMemberAndIsReadFalse(findMember);

        // hasNextPage
        boolean hasNextPage = contents.size() >= pageable.getPageSize();

        return new SliceCustom<>(contents, pageable, hasNextPage, unReadNotificationTotalCount);
    }

    private Long countByMemberAndIsReadFalse(Member member) {
        return query.select(notification.count())
                .from(notification)
                .where(notification.member.eq(member)
                        .and(notification.isRead.isFalse()))
                .fetchOne();
    }

    private BooleanExpression getCursorCondition(Long notificationId) {
        if (notificationId == null) {
            return null;
        }

        return notification.id.lt(notificationId);
    }
}
