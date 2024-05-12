package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.response.BookmarkResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleDetailResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleMainResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public interface TechArticleService {
    Slice<TechArticleMainResponse> getTechArticles(Pageable pageable, String elasticId, TechArticleSort techArticleSort,
                                                   String keyword, Float score, Authentication authentication);

    TechArticleDetailResponse getTechArticle(Long id, Authentication authentication);

    BookmarkResponse updateBookmark(Long id, boolean status, Authentication authentication);

    Slice<TechArticleMainResponse> getBookmarkedTechArticles(Pageable pageable, Long techArticleId,
                                                             BookmarkSort bookmarkSort, Authentication authentication);
}