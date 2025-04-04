package com.dreamypatisiel.devdevdev.domain.repository.notification;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Notification;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMemberInAndTechArticleIdInOrderByMemberDesc(Set<Member> members, Set<Long> techArticleIds);

    Long countByMemberAndIsReadIsFalse(Member member);
}
