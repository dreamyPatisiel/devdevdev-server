package com.dreamypatisiel.devdevdev.domain.repository.pick.custom;

import com.dreamypatisiel.devdevdev.domain.entity.PickReply;
import java.util.Optional;

public interface PickReplyRepositoryCustom {
    Optional<PickReply> findWithPickWithPickCommentByIdAndPickCommentIdAndPickIdAndCreatedByIdAndDeletedAtIsNull(Long id, Long pickId,
                                                                                                                 Long pickCommentId,
                                                                                                                 Long createdById);
}
