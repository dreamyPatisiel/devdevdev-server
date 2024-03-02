package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public interface TechArticleService {
    Slice<TechArticleResponse> getTechArticles(Pageable pageable, String keyword, Authentication authentication);
}