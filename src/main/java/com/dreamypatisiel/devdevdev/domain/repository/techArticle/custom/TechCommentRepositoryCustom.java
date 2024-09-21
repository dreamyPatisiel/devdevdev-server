package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentSort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface TechCommentRepositoryCustom {
    Slice<TechComment> findOriginParentTechCommentsByCursor(Long techArticleId, Long techCommentId,
                                                            TechCommentSort techCommentSort, Pageable pageable);
}
