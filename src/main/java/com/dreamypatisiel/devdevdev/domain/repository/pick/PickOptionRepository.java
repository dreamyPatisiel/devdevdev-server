package com.dreamypatisiel.devdevdev.domain.repository.pick;

import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PickOptionRepository extends JpaRepository<PickOption, Long> {

    @Modifying
    @Query("delete from PickOption po where po.id in :pickOptionIds")
    void deleteAllByPickOptionIdIn(List<Long> pickOptionIds);
}
