package com.dreamypatisiel.devdevdev.domain.repository.pick;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.PickReply;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PickReplyRepositoryTest {

    @Autowired
    PickReplyRepository pickReplyRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("픽픽픽 답글 조회 쿼리 테스트(픽픽픽, 픽픽픽 답글 페치조인)")
    void findWithPickWithPickCommentByIdAndPickCommentIdAndPickIdAndCreatedByIdAndDeletedAtIsNull() {
        // given
        // 회원 생성
        Member member = Member.builder()
                .name("회원")
                .isDeleted(false)
                .build();
        em.persist(member);

        // 픽픽픽 생성
        Pick pick = Pick.builder()
                .title(new Title("픽픽픽"))
                .build();
        em.persist(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = PickComment.builder()
                .isPublic(false)
                .pick(pick)
                .createdBy(member)
                .build();
        em.persist(pickComment);

        // 픽픽픽 답글 생성
        PickReply pickReply = PickReply.builder()
                .pickComment(pickComment)
                .createdBy(member)
                .build();
        pickReplyRepository.save(pickReply);

        em.flush();
        em.clear();

        // when
        PickReply findPickReply = pickReplyRepository.findWithPickWithPickCommentByIdAndPickCommentIdAndPickIdAndCreatedByIdAndDeletedAtIsNull(
                pickReply.getId(), pickComment.getId(), pick.getId(), member.getId()).get();

        // then
        assertAll(
                () -> assertThat(findPickReply.getId()).isEqualTo(pickReply.getId()),
                () -> assertThat(findPickReply.getPickComment().getId()).isEqualTo(pickComment.getId()),
                () -> assertThat(findPickReply.getCreatedBy().getId()).isEqualTo(member.getId())
        );
    }
}