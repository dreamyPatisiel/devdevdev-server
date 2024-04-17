package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.response.BookmarkResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public interface TechArticleService {
    Slice<TechArticleResponse> getTechArticles(Pageable pageable, String elasticId, TechArticleSort techArticleSort,
                                                  String keyword, Float score, Authentication authentication);

    TechArticleResponse getTechArticle(Long id, Authentication authentication);

    BookmarkResponse updateBookmark(Long id, boolean status, Authentication authentication);
}