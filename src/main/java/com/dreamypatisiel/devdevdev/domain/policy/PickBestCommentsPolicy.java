package com.dreamypatisiel.devdevdev.domain.policy;

import org.springframework.stereotype.Component;

@Component
public class PickBestCommentsPolicy implements BestCommentPolicy {

    public static final int BEST_COMMENTS_MAX_OFFSET = 10;
    public static final int BEST_COMMENTS_DEFAULT_OFFSET = 3;

    /**
     * @Note: 베스트 댓글 size 정책을 적용하여 예측 불가능한 자원을 낭비한다.(최소 3개 최대 10개)
     * @Author: 장세웅
     * @Since: 2024.10.09
     */
    public int applySize(int size) {
        if (size > BEST_COMMENTS_MAX_OFFSET) {
            return BEST_COMMENTS_DEFAULT_OFFSET;
        }

        return size;
    }
}
