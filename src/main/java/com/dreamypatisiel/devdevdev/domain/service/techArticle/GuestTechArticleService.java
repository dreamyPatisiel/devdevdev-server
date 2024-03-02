package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestTechArticleService implements TechArticleService {
    @Override
    public Slice<TechArticleResponse> getTechArticles(Pageable pageable, String keyword, Authentication authentication) {
        return null;
    }
}
