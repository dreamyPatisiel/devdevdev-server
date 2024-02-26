package com.dreamypatisiel.devdevdev.domain.repository;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickVoteRepository extends JpaRepository<PickVote, Long> {
    boolean existsByPickOptionAndMember(PickOption pickOption, Member member);
    boolean existsByMemberAndPick(Member member, Pick pick);
}
