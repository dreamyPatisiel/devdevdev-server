package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.response.BookmarkResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleDetailResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleMainResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechCommentRegisterResponse;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterTechCommentRequest;
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

    BookmarkResponse updateBookmark(Long techArticleId, boolean status, Authentication authentication);

    TechCommentRegisterResponse registerTechComment(Long techArticleId,
                                                    RegisterTechCommentRequest registerTechCommentRequest,
                                                    Authentication authentication);
}