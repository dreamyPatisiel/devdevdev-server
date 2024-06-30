package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface TechArticleRepositoryCustom {
    List<TechArticle> findAllByElasticIdIn(List<String> elasticIds);

    Slice<TechArticle> findBookmarkedByMemberAndCursor(Pageable pageable, Long techArticleId, BookmarkSort bookmarkSort,
                                                       Member member);
}
