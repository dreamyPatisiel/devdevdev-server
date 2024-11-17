package com.dreamypatisiel.devdevdev.domain.repository;

import com.dreamypatisiel.devdevdev.domain.entity.BlameType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlameTypeRepository extends JpaRepository<BlameType, Long> {
    List<BlameType> findAllByOrderBySortOrderAsc();
}
