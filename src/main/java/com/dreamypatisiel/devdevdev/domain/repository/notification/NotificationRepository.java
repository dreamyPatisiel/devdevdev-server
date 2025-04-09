package com.dreamypatisiel.devdevdev.domain.repository.notification;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import com.dreamypatisiel.devdevdev.domain.repository.notification.custom.NotificationRepositoryCustom;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long>,  NotificationRepositoryCustom {
    Optional<Notification> findByIdAndMember(Long id, Member member);

    List<Notification> findAllByMemberId(Long memberId);

    List<Notification> findByMemberInAndTechArticleIdInOrderByMemberDesc(Set<Member> members, Set<Long> techArticleIds);

    Long countByMemberAndIsReadIsFalse(Member member);
}
