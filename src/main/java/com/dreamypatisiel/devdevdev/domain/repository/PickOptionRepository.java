package com.dreamypatisiel.devdevdev.domain.repository;

import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickOptionRepository extends JpaRepository<PickOption, Long> {

    List<PickOption> findPickOptionsByIdIn(List<Long> pickOptionIds);
}
