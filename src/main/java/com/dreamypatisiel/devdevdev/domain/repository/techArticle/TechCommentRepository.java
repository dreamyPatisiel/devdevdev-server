package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechCommentRepository extends JpaRepository<TechComment, Long> {

    Optional<TechComment> findByIdAndTechArticleIdAndCreatedByIdAndDeletedAtIsNull(Long id, Long techArticleId,
                                                                                   Long createdById);

    Optional<TechComment> findByIdAndTechArticleIdAndDeletedAtIsNull(Long id, Long techArticleId);
}
