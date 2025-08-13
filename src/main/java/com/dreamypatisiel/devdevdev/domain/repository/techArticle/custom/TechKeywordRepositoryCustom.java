package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import com.dreamypatisiel.devdevdev.domain.entity.TechKeyword;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TechKeywordRepositoryCustom {
    List<TechKeyword> searchKeyword(String inputJamo, String inputChosung, Pageable pageable);
}
