package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechArticleRepository extends JpaRepository<Bookmark, Long>, TechArticleRepositoryCustom {
}
