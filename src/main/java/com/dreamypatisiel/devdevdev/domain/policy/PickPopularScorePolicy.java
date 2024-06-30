package com.dreamypatisiel.devdevdev.domain.policy;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import org.springframework.stereotype.Component;

@Component
public class PickPopularScorePolicy implements PopularScorePolicy {

    public static final long COMMENT_WEIGHT = 4L;
    public static final long VOTE_WEIGHT = 4L;
    public static final long VIEW_WEIGHT = 2L;
    public static final String INVALID_PICK_TYPE_MESSAGE = "올바른 Pick 타입이 아닙니다.";

    @Override
    public Count calculatePopularScore(Object object) {
        boolean isPickClassType = object instanceof Pick;
        if(!isPickClassType) {
            throw new IllegalArgumentException(INVALID_PICK_TYPE_MESSAGE);
        }

        Pick pick = (Pick) object;
        long commentScore = pick.getCommentTotalCount().getCount() * COMMENT_WEIGHT;
        long voteScore = pick.getVoteTotalCount().getCount() * VOTE_WEIGHT;
        long viewScore = pick.getViewTotalCount().getCount() * VIEW_WEIGHT;

        return new Count(commentScore + voteScore + viewScore);
    }
}
