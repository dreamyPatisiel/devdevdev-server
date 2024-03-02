package com.dreamypatisiel.devdevdev.domain.repository;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
}
