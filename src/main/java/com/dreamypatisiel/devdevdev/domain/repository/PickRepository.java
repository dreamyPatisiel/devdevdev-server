package com.dreamypatisiel.devdevdev.domain.repository;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickRepository extends JpaRepository<Pick, Long> {

    @EntityGraph(attributePaths = {"pickOptions", "member"})
    Slice<Pick> findByIdIsLessThanOrderByIdDesc(Pageable pageable, Long id);
}
