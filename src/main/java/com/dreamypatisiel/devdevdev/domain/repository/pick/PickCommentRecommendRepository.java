package com.dreamypatisiel.devdevdev.domain.repository.pick;

import com.dreamypatisiel.devdevdev.domain.entity.PickCommentRecommend;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickCommentRecommendRepository extends JpaRepository<PickCommentRecommend, Long> {

    Boolean existsByPickCommentIdAndMemberId(Long pickCommentId, Long memberId);

    Optional<PickCommentRecommend> findByPickCommentIdAndMemberId(Long pickCommentId, Long memberId);
}
