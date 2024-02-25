package com.dreamypatisiel.devdevdev.domain.repository.pick;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PickRepositoryCustom {
    Slice<Pick> findPicksByLoePickId(Pageable pageable, Long pickId, PickSort pickSort);
}
