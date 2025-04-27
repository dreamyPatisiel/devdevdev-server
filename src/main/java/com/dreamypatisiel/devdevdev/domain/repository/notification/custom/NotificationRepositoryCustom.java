package com.dreamypatisiel.devdevdev.domain.repository.notification.custom;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import java.util.List;
import java.util.Set;

public interface NotificationRepositoryCustom {
    void bulkMarkAllAsReadByMemberId(Long memberId);

    List<Notification> findByMemberInAndTechArticleIdInOrderByNull(Set<Member> members, Set<Long> techArticleIds);
}