package com.dreamypatisiel.devdevdev.domain.repository.blame.custom;

import static com.dreamypatisiel.devdevdev.domain.entity.QBlame.blame;

import com.dreamypatisiel.devdevdev.domain.service.blame.dto.BlameDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BlameRepositoryImpl implements BlameRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public Boolean existsBlameByBlameDto(BlameDto blameDto) {

        Integer fetchOne = query.selectOne()
                .from(blame)
                .where(existBlameCondition(blameDto))
                .fetchFirst(); // limit 1

        return fetchOne != null;
    }

    private BooleanExpression existBlameCondition(BlameDto blameDto) {

        Long memberId = blameDto.getMemberId();
        Long pickId = blameDto.getPickId();
        Long pickCommentId = blameDto.getPickCommentId();
        Long techArticleId = blameDto.getTechArticleId();
        Long techCommentId = blameDto.getTechCommentId();

        // 픽픽픽 게시글 신고
        if (blameDto.isBlamePick()) {
            return memberIdEq(memberId)
                    .and(pickIdEq(pickId))
                    .and(blame.pickComment.isNull());
        }

        // 픽픽픽 댓글 신고
        if (blameDto.isBlamePickComment()) {
            return memberIdEq(memberId)
                    .and(pickIdEq(pickId))
                    .and(blame.pickComment.id.eq(pickCommentId));
        }

        // 기술 블로그 댓글 신고
        if (blameDto.isBlameTechArticleComment()) {
            return memberIdEq(memberId)
                    .and(blame.techArticle.id.eq(techArticleId))
                    .and(blame.techComment.id.eq(techCommentId));
        }

        return null;
    }

    private static BooleanExpression pickIdEq(Long pickId) {
        return blame.pick.id.eq(pickId);
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return blame.member.id.eq(memberId);
    }
}
