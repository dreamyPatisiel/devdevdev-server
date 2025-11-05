package com.dreamypatisiel.devdevdev.domain.repository.pick.custom;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PickRepositoryCustom {
    Slice<Pick> findPicksByCursor(Pageable pageable, Long pickId, PickSort pickSort);

    Optional<Pick> findPickWithPickOptionWithPickVoteWithMemberByPickId(Long pickId);

    Optional<Pick> findPickWithPickOptionByPickId(Long pickId);

    Slice<Pick> findPicksByMemberAndCursor(Pageable pageable, Member member, Long pickId);

    List<Pick> findPicksWithPickOptionWithMemberByIdIn(Set<Long> ids);
}
