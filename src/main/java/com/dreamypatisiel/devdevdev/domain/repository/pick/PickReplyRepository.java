package com.dreamypatisiel.devdevdev.domain.repository.pick;

import com.dreamypatisiel.devdevdev.domain.entity.PickReply;
import com.dreamypatisiel.devdevdev.domain.repository.pick.custom.PickReplyRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickReplyRepository extends JpaRepository<PickReply, Long>, PickReplyRepositoryCustom {
    Optional<PickReply> findByIdAndCreatedByIdAndDeletedAtIsNull(Long id, Long createdById);
}
