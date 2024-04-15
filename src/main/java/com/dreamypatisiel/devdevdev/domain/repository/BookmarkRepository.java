package com.dreamypatisiel.devdevdev.domain.repository;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByTechArticleAndMember(TechArticle techArticle, Member member);
}
