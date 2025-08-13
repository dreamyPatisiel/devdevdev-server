package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechKeyword;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom.TechKeywordRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechKeywordRepository extends JpaRepository<TechKeyword, Long>, TechKeywordRepositoryCustom {
}