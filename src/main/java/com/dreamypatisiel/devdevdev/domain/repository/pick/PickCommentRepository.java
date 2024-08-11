package com.dreamypatisiel.devdevdev.domain.repository.pick;

import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickCommentRepository extends JpaRepository<PickComment, Long> {

    @EntityGraph(attributePaths = {"pick"})
    Optional<PickComment> findWithPickByIdAndPickIdAndMemberIdAndDeletedAtIsNull(Long id, Long pickId, Long memberId);
}
