package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface TechArticleRepositoryCustom {
    List<TechArticle> findAllByElasticIdIn(List<String> elasticIds);

    Slice<TechArticle> findBookmarkedByCursor(Pageable pageable, Long techArticleId, BookmarkSort bookmarkSort, Member member);
}
