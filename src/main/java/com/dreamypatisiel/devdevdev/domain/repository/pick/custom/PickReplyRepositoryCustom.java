package com.dreamypatisiel.devdevdev.domain.repository.pick.custom;

import com.dreamypatisiel.devdevdev.domain.entity.PickReply;
import java.util.Optional;

public interface PickReplyRepositoryCustom {
    Optional<PickReply> findWithPickWithPickCommentByIdAndPickCommentIdAndPickIdAndCreatedById(Long id, Long pickId,
                                                                                               Long pickCommentId,
                                                                                               Long createdById);
}
