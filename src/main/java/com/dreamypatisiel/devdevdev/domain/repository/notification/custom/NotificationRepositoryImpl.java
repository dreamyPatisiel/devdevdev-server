package com.dreamypatisiel.devdevdev.domain.repository.notification.custom;

import static com.dreamypatisiel.devdevdev.domain.entity.QNotification.notification;
import static com.querydsl.core.types.dsl.Expressions.stringTemplate;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;

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
    public List<Notification> findByMemberInAndTechArticleIdInOrderByNull(Set<Member> members, Set<Long> techArticleIds) {

        return query.selectFrom(notification)
                .where(notification.member.in(members)
                        .and(notification.techArticle.id.in(techArticleIds)))
                .orderBy(stringTemplate("null").asc())
                .fetch();
    }
}
