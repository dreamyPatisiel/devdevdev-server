package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom.TechCommentRepositoryCustom;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechCommentRepository extends JpaRepository<TechComment, Long>, TechCommentRepositoryCustom {

    @EntityGraph(attributePaths = {"techArticle"})
    Optional<TechComment> findWithTechArticleByIdAndTechArticleId(Long id, Long techArticleId);

    Optional<TechComment> findByIdAndTechArticleIdAndCreatedByIdAndDeletedAtIsNull(Long id, Long techArticleId,
                                                                                   Long createdById);

    Optional<TechComment> findByIdAndTechArticleIdAndDeletedAtIsNull(Long id, Long techArticleId);

    @EntityGraph(attributePaths = {"createdBy", "deletedBy", "techArticle"})
    List<TechComment> findWithMemberWithTechArticleByOriginParentIdInAndParentIsNotNullAndOriginParentIsNotNull(
            Set<Long> originParentIds);

    Long countByTechArticleIdAndParentIsNull(Long techArticleId);
}
