package com.dreamypatisiel.devdevdev.domain.repository;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnonymousMemberRepository extends JpaRepository<AnonymousMember, Long> {
    Optional<AnonymousMember> findByAnonymousMemberId(String anonymousMemberId);
}
