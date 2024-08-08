package com.dreamypatisiel.devdevdev.domain.repository.pick;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.repository.pick.custom.PickRepositoryCustom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickRepository extends JpaRepository<Pick, Long>, PickRepositoryCustom {

    @EntityGraph(attributePaths = {"pickOptions"})
    Optional<Pick> findPickWithPickOptionByIdAndMember(Long id, Member member);

    List<Pick> findTop1000ByContentStatusAndEmbeddingsIsNotNullOrderByCreatedAtDesc(ContentStatus contentStatus);

    @EntityGraph(attributePaths = {"pickVotes"})
    Optional<Pick> findWithPickVoteById(Long id);
}
