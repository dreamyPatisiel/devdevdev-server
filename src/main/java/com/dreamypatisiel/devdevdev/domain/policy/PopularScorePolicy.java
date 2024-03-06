package com.dreamypatisiel.devdevdev.domain.policy;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;

public interface PopularScorePolicy {
    Count calculatePopularScore(Object object);
}
