package com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.BookmarkResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public interface TechArticleService {
    Slice<TechArticleMainResponse> getTechArticles(Pageable pageable, String elasticId, TechArticleSort techArticleSort,
                                                   String keyword, Long companyId, Float score,
                                                   Authentication authentication);

    TechArticleDetailResponse getTechArticle(Long techArticleId, Authentication authentication);

    BookmarkResponse updateBookmark(Long techArticleId, Authentication authentication);
}