package com.dreamypatisiel.devdevdev.domain.repository.pick;

import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.repository.pick.custom.PickCommentRepositoryCustom;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PickCommentRepository extends JpaRepository<PickComment, Long>, PickCommentRepositoryCustom {

    @EntityGraph(attributePaths = {"pick"})
    Optional<PickComment> findWithPickByIdAndPickIdAndCreatedByIdAndDeletedAtIsNull(Long id, Long pickId,
                                                                                    Long createdById);

    @EntityGraph(attributePaths = {"pick"})
    Optional<PickComment> findWithPickByIdAndPickIdAndCreatedAnonymousByIdAndDeletedAtIsNull(Long id, Long pickId,
                                                                                             Long createdAnonymousById);

    Optional<PickComment> findByIdAndPickIdAndDeletedAtIsNull(Long id, Long pickId);

    @EntityGraph(attributePaths = {"pick"})
    Optional<PickComment> findWithPickByIdAndPickId(Long id, Long pickId);

    @EntityGraph(attributePaths = {"createdBy", "deletedBy", "createdAnonymousBy", "deletedAnonymousBy", "pickVote", "pick",
            "pick.member", "pickCommentRecommends"})
    List<PickComment> findWithDetailsByOriginParentIdInAndParentIsNotNullAndOriginParentIsNotNull(
            Set<Long> originParentIds);

    @Modifying
    @Query("delete from PickComment pc where pc.pick.id =:pickId")
    void deleteAllByPickId(Long pickId);

    Long countByOriginParentIdIn(Set<Long> pickIds);

    Long countByPickIdAndOriginParentIsNullAndParentIsNullAndDeletedAtIsNull(Long pickId);

    Long countByCreatedByIdAndDeletedAtIsNull(Long createdById);
}
