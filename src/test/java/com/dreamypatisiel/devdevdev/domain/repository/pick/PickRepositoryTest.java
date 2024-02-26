package com.dreamypatisiel.devdevdev.domain.repository.pick;

import static org.assertj.core.api.Assertions.assertThat;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.repository.PickOptionRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PickRepositoryTest {

    @Autowired
    PickRepository pickRepository;
    @Autowired
    PickOptionRepository pickOptionRepository;
    @Autowired
    EntityManager em;

    String thumbnailUrl = "픽1 섬네일 이미지 url";
    String author = "운영자";

    @Test
    @DisplayName("findPicksByLoePickId 쿼리 확인")
    void findPicksByLtPickId() {
        // given
        PickOption pickOption1 = PickOption.create(new Title("픽옵션1"), new PickContents("픽콘텐츠1"), new Count(1));
        PickOption pickOption2 = PickOption.create(new Title("픽옵션2"), new PickContents("픽콘텐츠2"), new Count(2));
        PickOption pickOption3 = PickOption.create(new Title("픽옵션3"), new PickContents("픽콘텐츠3"), new Count(3));
        PickOption pickOption4 = PickOption.create(new Title("픽옵션4"), new PickContents("픽콘텐츠4"), new Count(4));

        Count pick1VoteTotalCount = new Count(pickOption1.getVoteTotalCount().getCount() + pickOption2.getVoteTotalCount().getCount());
        Count pick2VoteTotalCount = new Count(pickOption1.getVoteTotalCount().getCount() + pickOption2.getVoteTotalCount().getCount());
        Count pick1ViewTotalCount = new Count(1);
        Count pick2ViewTotalCount = new Count(1);
        Count pick1CommentTotalCount = new Count(0);
        Count pick2CommentTotalCount = new Count(0);
        Pick pick1 = Pick.create(new Title("픽1타이틀"), pick1VoteTotalCount, pick1ViewTotalCount, pick1CommentTotalCount,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick2 = Pick.create(new Title("픽2타이틀"), pick2VoteTotalCount, pick2ViewTotalCount, pick2CommentTotalCount,
                thumbnailUrl, author, List.of(pickOption3, pickOption4), List.of());
        Pick pick3 = Pick.create(new Title("픽3타이틀"), pick2VoteTotalCount, pick2ViewTotalCount, pick2CommentTotalCount,
                thumbnailUrl, author, List.of(pickOption3, pickOption4), List.of());
        Pick pick4 = Pick.create(new Title("픽4타이틀"), pick2VoteTotalCount, pick2ViewTotalCount, pick2CommentTotalCount,
                thumbnailUrl, author, List.of(pickOption3, pickOption4), List.of());
        Pick pick5 = Pick.create(new Title("픽5타이틀"), pick2VoteTotalCount, pick2ViewTotalCount, pick2CommentTotalCount,
                thumbnailUrl, author, List.of(pickOption3, pickOption4), List.of());
        Pick pick6 = Pick.create(new Title("픽6타이틀"), pick2VoteTotalCount, pick2ViewTotalCount, pick2CommentTotalCount,
                thumbnailUrl, author, List.of(pickOption3, pickOption4), List.of());
        Pick pick7 = Pick.create(new Title("픽7타이틀"), pick2VoteTotalCount, pick2ViewTotalCount, pick2CommentTotalCount,
                thumbnailUrl, author, List.of(pickOption3, pickOption4), List.of());
        pickRepository.saveAll(List.of(pick1, pick2, pick3, pick4, pick5, pick6, pick7));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2, pickOption3, pickOption4));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Pick> picks = pickRepository.findPicksByLoePickId(pageable, null, null);

        // then
        assertThat(picks).hasSize(7);
    }

    @Test
    @DisplayName("조회수 내림차순으로 Pick을 조회한다.")
    void findPicksByLtPickIdOrderByViewCountDesc() {
        // given
        PickOption pickOption1 = PickOption.create(new Title("픽옵션1"), new PickContents("픽콘텐츠1"), new Count(1));
        PickOption pickOption2 = PickOption.create(new Title("픽옵션2"), new PickContents("픽콘텐츠2"), new Count(2));

        Count pick1ViewTotalCount = new Count(1);
        Count pick2ViewTotalCount = new Count(2);
        Count pick3ViewTotalCount = new Count(3);
        Count count = new Count(1);
        Pick pick1 = Pick.create(new Title("픽1타이틀"), count, pick1ViewTotalCount, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick2 = Pick.create(new Title("픽2타이틀"), count, pick2ViewTotalCount, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick3 = Pick.create(new Title("픽3타이틀"), count, pick3ViewTotalCount, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());

        pickRepository.saveAll(List.of(pick1, pick2, pick3));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Pick> picks = pickRepository.findPicksByLoePickId(pageable, null, PickSort.MOST_VIEWED);

        // then
        assertThat(picks).hasSize(3)
                .extracting(Pick::getTitle)
                .containsExactly(
                        new Title("픽3타이틀"), new Title("픽2타이틀"),new Title("픽1타이틀")
                );
    }

    @Test
    @DisplayName("생성시간 내림차순으로 Pick을 조회한다.")
    void findPicksByLtPickIdOrderByCreatedAtDesc() {
        // given
        PickOption pickOption1 = PickOption.create(new Title("픽옵션1"), new PickContents("픽콘텐츠1"), new Count(1));
        PickOption pickOption2 = PickOption.create(new Title("픽옵션2"), new PickContents("픽콘텐츠2"), new Count(2));

        Count count = new Count(1);
        Pick pick1 = Pick.create(new Title("픽1타이틀"), count, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick2 = Pick.create(new Title("픽2타이틀"), count, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick3 = Pick.create(new Title("픽3타이틀"), count, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());

        pickRepository.saveAll(List.of(pick1, pick2, pick3));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Pick> picks = pickRepository.findPicksByLoePickId(pageable, null, PickSort.LATEST);

        // then
        assertThat(picks).hasSize(3)
                .extracting(Pick::getTitle)
                .containsExactly(
                        new Title("픽3타이틀"), new Title("픽2타이틀"),new Title("픽1타이틀")
                );
    }

    @Test
    @DisplayName("댓글수 내림차순으로 Pick을 조회한다.")
    void findPicksByLtPickIdOrderByDesc() {
        // given
        PickOption pickOption1 = PickOption.create(new Title("픽옵션1"), new PickContents("픽콘텐츠1"), new Count(1));
        PickOption pickOption2 = PickOption.create(new Title("픽옵션2"), new PickContents("픽콘텐츠2"), new Count(2));

        Count pick1commentTotalCount = new Count(1);
        Count pick2commentTotalCount = new Count(2);
        Count pick3commentTotalCount = new Count(3);
        Count count = new Count(1);
        Pick pick1 = Pick.create(new Title("픽1타이틀"), count, count, pick1commentTotalCount,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick2 = Pick.create(new Title("픽2타이틀"), count, count, pick2commentTotalCount,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick3 = Pick.create(new Title("픽3타이틀"), count, count, pick3commentTotalCount,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());

        pickRepository.saveAll(List.of(pick1, pick2, pick3));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Pick> picks = pickRepository.findPicksByLoePickId(pageable, null, PickSort.MOST_COMMENTED);

        // then
        assertThat(picks).hasSize(3)
                .extracting(Pick::getTitle)
                .containsExactly(
                        new Title("픽3타이틀"), new Title("픽2타이틀"),new Title("픽1타이틀")
                );
    }

    @Test
    @DisplayName("인기순으로 Pick을 조회한다."
            + "(현재 가중치 = 댓글수:"+PickSort.COMMENT_WEIGHT+", 투표수:"+PickSort.VOTE_WEIGHT+", 조회수:"+PickSort.VIEW_WEIGHT+")")
    void findPicksByLtPickIdOrderByPopular() {
        // given
        PickOption pickOption1 = PickOption.create(new Title("픽옵션1"), new PickContents("픽콘텐츠1"), new Count(1));
        PickOption pickOption2 = PickOption.create(new Title("픽옵션2"), new PickContents("픽콘텐츠2"), new Count(2));

        Count pick1commentTotalCount = new Count(2);
        Count pick1VoteTotalCount = new Count(2);
        Count pick1ViewTotalCount = new Count(1);

        Count pick2commentTotalCount = new Count(1);
        Count pick2VoteTotalCount = new Count(2);
        Count pick2ViewTotalCount = new Count(2);

        Count pick3commentTotalCount = new Count(1);
        Count pick3VoteTotalCount = new Count(1);
        Count pick3ViewTotalCount = new Count(2);

        Pick pick1 = Pick.create(new Title("픽1타이틀"), pick1VoteTotalCount, pick1ViewTotalCount, pick1commentTotalCount,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick2 = Pick.create(new Title("픽2타이틀"), pick2VoteTotalCount, pick2ViewTotalCount, pick2commentTotalCount,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick3 = Pick.create(new Title("픽3타이틀"), pick3VoteTotalCount, pick3ViewTotalCount, pick3commentTotalCount,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());

        pickRepository.saveAll(List.of(pick1, pick2, pick3));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Pick> picks = pickRepository.findPicksByLoePickId(pageable, null, PickSort.POPULAR);

        // then
        assertThat(picks).hasSize(3)
                .extracting(Pick::getTitle)
                .containsExactly(
                        new Title("픽1타이틀"), new Title("픽2타이틀"),new Title("픽3타이틀")
                );
    }
}