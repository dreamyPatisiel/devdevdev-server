package com.dreamypatisiel.devdevdev.domain.repository.pick;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PickVoteRepository extends JpaRepository<PickVote, Long> {

    @EntityGraph(attributePaths = {"pick", "pickOption"})
    Optional<PickVote> findByPickIdAndMember(Long pickId, Member member);

    @EntityGraph(attributePaths = {"pick", "pickOption"})
    Optional<PickVote> findByPickIdAndAnonymousMember(Long pickId, AnonymousMember anonymousMember);

    @Modifying
    @Query("delete from PickVote pv where pv.pickOption.id in :pickOptionIds")
    void deleteAllByPickOptionIn(List<Long> pickOptionIds);
}
