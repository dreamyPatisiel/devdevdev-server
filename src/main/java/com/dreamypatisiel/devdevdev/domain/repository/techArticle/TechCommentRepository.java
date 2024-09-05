package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechCommentRepository extends JpaRepository<TechComment, Long> {

    @EntityGraph(attributePaths = {"techArticle"})
    Optional<TechComment> findWithTechArticleByIdAndTechArticleIdAndCreatedByIdAndDeletedAtIsNull(Long id, Long techArticleId,
                                                                                   Long createdById);

    @EntityGraph(attributePaths = {"techArticle"})
    Optional<TechComment> findWithTechArticleByIdAndTechArticleIdAndDeletedAtIsNull(Long id, Long techArticleId);

    @EntityGraph(attributePaths = {"techArticle"})
    Optional<TechComment> findWithTechArticleByIdAndTechArticleId(Long id, Long techArticleId);
}
