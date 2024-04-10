package com.dreamypatisiel.devdevdev.domain.repository.pick;

import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickOptionImageRepository extends JpaRepository<PickOptionImage, Long> {
    List<PickOptionImage> findByIdIn(List<Long> ids);
}
