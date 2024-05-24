package com.dreamypatisiel.devdevdev.domain.repository.pick.custom;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PickRepositoryCustom {
    Slice<Pick> findPicksByCursor(Pageable pageable, Long pickId, PickSort pickSort);

    Optional<Pick> findPickWithPickOptionWithPickVoteWithMemberByPickId(Long pickId);

    Optional<Pick> findPickWithPickOptionByPickId(Long pickId);

    Slice<Pick> findPicksByMemberAndCursor(Pageable pageable, Member member, Long pickId);
}
