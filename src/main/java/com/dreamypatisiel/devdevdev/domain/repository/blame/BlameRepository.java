package com.dreamypatisiel.devdevdev.domain.repository.blame;

import com.dreamypatisiel.devdevdev.domain.entity.Blame;
import com.dreamypatisiel.devdevdev.domain.repository.blame.custom.BlameRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlameRepository extends JpaRepository<Blame, Long>, BlameRepositoryCustom {
    Boolean existsByMemberIdAndPickId(Long memberId, Long pickId);
}
