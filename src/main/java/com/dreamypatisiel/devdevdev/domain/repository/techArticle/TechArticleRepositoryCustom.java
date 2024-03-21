package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;

import java.util.List;

public interface TechArticleRepositoryCustom {
    List<TechArticle> findAllByElasticIdIn(List<String> elasticIds);
}
