package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TechArticleDto {
    private final TechArticle techArticle;
    private final Double score;
    
    public static TechArticleDto of(TechArticle techArticle, Double score) {
        return new TechArticleDto(techArticle, score);
    }
}
