package com.dreamypatisiel.devdevdev.domain.repository.pick;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickVoteRepository extends JpaRepository<PickVote, Long> {
    boolean existsByPickOptionAndMember(PickOption pickOption, Member member);

    boolean existsByMemberAndPick(Member member, Pick pick);

    @EntityGraph(attributePaths = {"pick", "pickOption"})
    Optional<PickVote> findByPickIdAndMember(Long pickId, Member member);
}
