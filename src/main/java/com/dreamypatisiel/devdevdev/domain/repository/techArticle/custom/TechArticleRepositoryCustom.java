package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface TechArticleRepositoryCustom {
    Slice<TechArticle> findBookmarkedByMemberAndCursor(Pageable pageable, Long techArticleId, BookmarkSort bookmarkSort,
                                                       Member member);

    SliceCustom<TechArticleDto> findTechArticlesByCursor(Pageable pageable, Long techArticleId, TechArticleSort techArticleSort,
                                                      Long companyId, String keyword, Double score);
}
