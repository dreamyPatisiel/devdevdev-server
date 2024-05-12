package com.dreamypatisiel.devdevdev.domain.repository.pick;

import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickOptionRepository extends JpaRepository<PickOption, Long> {

    List<PickOption> findPickOptionsByIdIn(List<Long> pickOptionIds);

    @EntityGraph(attributePaths = {"pick"})
    Optional<PickOption> findPickOptionAndPickById(Long pickOptionId);
}
