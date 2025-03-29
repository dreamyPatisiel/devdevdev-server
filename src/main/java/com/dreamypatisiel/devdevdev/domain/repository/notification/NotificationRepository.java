package com.dreamypatisiel.devdevdev.domain.repository.notification;

import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByIdAndMemberId(Long id, Long memberId);
}
