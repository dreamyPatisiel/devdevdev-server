package com.dreamypatisiel.devdevdev.domain.repository.pick;

import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PickOptionImageRepository extends JpaRepository<PickOptionImage, Long> {
    List<PickOptionImage> findByIdIn(List<Long> ids);

    @Modifying
    @Query("delete from PickOptionImage poi where poi.pickOption.id in :ids")
    void deleteAllByPickOptionIn(List<Long> ids);
}
