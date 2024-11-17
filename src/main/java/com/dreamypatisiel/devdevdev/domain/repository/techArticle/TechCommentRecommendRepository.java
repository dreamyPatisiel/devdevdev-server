package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechCommentRecommend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TechCommentRecommendRepository extends JpaRepository<TechCommentRecommend, Long> {
    Optional<TechCommentRecommend> findByTechCommentIdAndMemberId(Long techCommentId, Long memberId);
}
