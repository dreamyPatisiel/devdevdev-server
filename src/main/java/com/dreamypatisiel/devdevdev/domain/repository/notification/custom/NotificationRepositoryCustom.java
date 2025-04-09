package com.dreamypatisiel.devdevdev.domain.repository.notification.custom;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryCustom {
    void bulkMarkAllAsReadByMemberId(Long memberId);

    SliceCustom<Notification> findNotificationsByMemberOrderByCreatedAtDesc(Pageable pageable, Member member);
}