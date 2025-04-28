package com.dreamypatisiel.devdevdev.domain.repository.notification.custom;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.querydsl.core.types.dsl.BooleanExpression;

import static com.dreamypatisiel.devdevdev.domain.entity.QNotification.notification;
import static com.querydsl.core.types.dsl.Expressions.stringTemplate;

import com.querydsl.jpa.JPQLQueryFactory;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
    public SliceCustom<Notification> findNotificationsByMemberAndTypeOrderByCreatedAtDesc(Pageable pageable,
                                                                                   List<NotificationType> notificationTypes,
                                                                                   Member member) {
        List<Notification> contents = query.selectFrom(notification)
                .where(notification.member.eq(member)
                        .and(notification.type.in(notificationTypes)))
                .orderBy(notification.createdAt.desc(), notification.id.desc())
                .limit(pageable.getPageSize())
                .fetch();

        // 회원이 읽지 않은 알림 개수
        long unReadNotificationTotalCount = countByMemberAndIsReadFalse(member);

        return new SliceCustom<>(contents, pageable, false, unReadNotificationTotalCount);
    }

    @Override
    public SliceCustom<Notification> findNotificationsByMemberAndCursor(Pageable pageable, Long notificationId, Member member) {
        List<Notification> contents = query.selectFrom(notification)
                .where(notification.member.eq(member)
                        .and(getCursorCondition(notificationId)))
                .orderBy(notification.createdAt.desc(), notification.id.desc())
                .limit(pageable.getPageSize())
                .fetch();

        // 회원이 읽지 않은 알림 개수
        long unReadNotificationTotalCount = countByMemberAndIsReadFalse(member);

        return new SliceCustom<>(contents, pageable, unReadNotificationTotalCount);
    }
  
    @Override
    public List<Notification> findByMemberInAndTechArticleIdInOrderByNull(Set<Member> members, Set<Long> techArticleIds) {

        return query.selectFrom(notification)
                .where(notification.member.in(members)
                        .and(notification.techArticle.id.in(techArticleIds)))
                .orderBy(stringTemplate("null").asc())
                .fetch();
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
