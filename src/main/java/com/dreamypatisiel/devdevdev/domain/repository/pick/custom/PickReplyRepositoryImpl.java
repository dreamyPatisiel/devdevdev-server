package com.dreamypatisiel.devdevdev.domain.repository.pick.custom;

import static com.dreamypatisiel.devdevdev.domain.entity.QPick.pick;
import static com.dreamypatisiel.devdevdev.domain.entity.QPickComment.pickComment;
import static com.dreamypatisiel.devdevdev.domain.entity.QPickReply.pickReply;

import com.dreamypatisiel.devdevdev.domain.entity.PickReply;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PickReplyRepositoryImpl implements PickReplyRepositoryCustom {

    private final JPQLQueryFactory query;

    @Override
    public Optional<PickReply> findWithPickWithPickCommentByIdAndPickCommentIdAndPickIdAndCreatedByIdAndDeletedAtIsNull(
            Long id, Long pickCommentId, Long pickId, Long createdById) {

        PickReply findPickReply = query.selectFrom(pickReply)
                .innerJoin(pickReply.pickComment, pickComment).fetchJoin()
                .innerJoin(pickReply.pickComment.pick, pick).fetchJoin()
                .where(pickReply.id.eq(id)
                        .and(pickComment.id.eq(pickCommentId))
                        .and(pickReply.createdBy.id.eq(createdById))
                        .and(pick.id.eq(pickId))
                        .and(pickReply.deletedAt.isNull()))
                .fetchOne();

        return Optional.ofNullable(findPickReply);
    }

    @Override
    public Optional<PickReply> findByIdAndPickCommentIdAndPickIdAndDeletedAtIsNull(
            Long id, Long pickCommentId, Long pickId) {
        
        PickReply findPickReply = query.selectFrom(pickReply)
                .innerJoin(pickReply.pickComment, pickComment)
                .innerJoin(pickReply.pickComment.pick, pick)
                .where(pickReply.id.eq(id)
                        .and(pickComment.id.eq(pickCommentId))
                        .and(pick.id.eq(pickId))
                        .and(pickReply.deletedAt.isNull()))
                .fetchOne();

        return Optional.ofNullable(findPickReply);
    }
}
