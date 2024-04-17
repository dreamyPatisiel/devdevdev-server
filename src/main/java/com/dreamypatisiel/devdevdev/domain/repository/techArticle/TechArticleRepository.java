package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom.TechArticleRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechArticleRepository extends JpaRepository<TechArticle, Long>, TechArticleRepositoryCustom {
}
