package com.dreamypatisiel.devdevdev.domain.repository.notification;

import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.domain.repository.notification.custom.NotificationRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {
    Optional<Notification> findByIdAndMemberId(Long id, Long memberId);

    List<Notification> findAllByMemberId(Long memberId);
}
