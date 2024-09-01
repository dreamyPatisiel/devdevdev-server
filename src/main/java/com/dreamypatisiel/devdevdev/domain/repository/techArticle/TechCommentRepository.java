package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechCommentRepository extends JpaRepository<TechComment, Long> {

    Optional<TechComment> findWithTechArticleByIdAndTechArticleIdAndCreatedByIdAndDeletedAtIsNull(Long id, Long techArticleId,
                                                                                   Long createdById);

    Optional<TechComment> findWithTechArticleByIdAndTechArticleIdAndDeletedAtIsNull(Long id, Long techArticleId);

    Optional<TechComment> findWithTechArticleByIdAndTechArticleId(Long id, Long techArticleId);
}
