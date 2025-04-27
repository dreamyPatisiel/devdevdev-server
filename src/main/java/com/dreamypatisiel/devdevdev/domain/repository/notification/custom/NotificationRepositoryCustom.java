package com.dreamypatisiel.devdevdev.domain.repository.notification.custom;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationRepositoryCustom {
    void bulkMarkAllAsReadByMemberId(Long memberId);

    SliceCustom<Notification> findNotificationsByMemberAndTypeOrderByCreatedAtDesc(
            Pageable pageable, List<NotificationType> notificationTypes, Member member);

    SliceCustom<Notification> findNotificationsByMemberAndCursor(Pageable pageable, Long notificationId, Member member);
}