package com.dreamypatisiel.devdevdev.domain.repository.pick;

import com.dreamypatisiel.devdevdev.domain.entity.PickCommentRecommend;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PickCommentRecommendRepository extends JpaRepository<PickCommentRecommend, Long> {

    Boolean existsByPickCommentIdAndMemberId(Long pickCommentId, Long memberId);

    Optional<PickCommentRecommend> findByPickCommentIdAndMemberId(Long pickCommentId, Long memberId);

    @EntityGraph(attributePaths = {"member"})
    List<PickCommentRecommend> findByMemberIdAndPickCommentIdIn(Long memberId, Set<Long> pickCommentIds);

    @Modifying
    @Query("delete from PickCommentRecommend pcr where pcr.pickComment.id in :pickCommentIds")
    void deleteAllByPickCommentIdIn(List<Long> pickCommentIds);
}
