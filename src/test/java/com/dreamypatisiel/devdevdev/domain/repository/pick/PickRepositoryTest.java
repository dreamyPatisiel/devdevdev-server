package com.dreamypatisiel.devdevdev.domain.repository.pick;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
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
    PickPopularScorePolicy pickPopularScorePolicy;
    @Autowired
    PickOptionImageRepository pickOptionImageRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EntityManager em;

    String thumbnailUrl = "픽1 섬네일 이미지 url";
    String author = "운영자";

    @Test
    @DisplayName("findPicksByCursor 쿼리 확인")
    void findPicksByCursor() {
        // given
        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"), new Count(1),
                PickOptionType.firstPickOption);
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"), new Count(2),
                PickOptionType.secondPickOption);
        PickOption pickOption3 = createPickOption(new Title("픽옵션3"), new PickOptionContents("픽콘텐츠3"), new Count(3),
                PickOptionType.firstPickOption);
        PickOption pickOption4 = createPickOption(new Title("픽옵션4"), new PickOptionContents("픽콘텐츠4"), new Count(4),
                PickOptionType.secondPickOption);

        Count pick1VoteTotalCount = new Count(
                pickOption1.getVoteTotalCount().getCount() + pickOption2.getVoteTotalCount().getCount());
        Count pick2VoteTotalCount = new Count(
                pickOption1.getVoteTotalCount().getCount() + pickOption2.getVoteTotalCount().getCount());
        Count pick1ViewTotalCount = new Count(1);
        Count pick2ViewTotalCount = new Count(1);
        Count pick1CommentTotalCount = new Count(0);
        Count pick2CommentTotalCount = new Count(0);
        Pick pick1 = createPick(new Title("픽1타이틀"), pick1VoteTotalCount, pick1ViewTotalCount, pick1CommentTotalCount,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick2 = createPick(new Title("픽2타이틀"), pick2VoteTotalCount, pick2ViewTotalCount, pick2CommentTotalCount,
                thumbnailUrl, author, List.of(pickOption3, pickOption4), List.of());
        Pick pick3 = createPick(new Title("픽3타이틀"), pick2VoteTotalCount, pick2ViewTotalCount, pick2CommentTotalCount,
                thumbnailUrl, author, List.of(pickOption3, pickOption4), List.of());
        Pick pick4 = createPick(new Title("픽4타이틀"), pick2VoteTotalCount, pick2ViewTotalCount, pick2CommentTotalCount,
                thumbnailUrl, author, List.of(pickOption3, pickOption4), List.of());
        Pick pick5 = createPick(new Title("픽5타이틀"), pick2VoteTotalCount, pick2ViewTotalCount, pick2CommentTotalCount,
                thumbnailUrl, author, List.of(pickOption3, pickOption4), List.of());
        Pick pick6 = createPick(new Title("픽6타이틀"), pick2VoteTotalCount, pick2ViewTotalCount, pick2CommentTotalCount,
                thumbnailUrl, author, List.of(pickOption3, pickOption4), List.of());
        Pick pick7 = createPick(new Title("픽7타이틀"), pick2VoteTotalCount, pick2ViewTotalCount, pick2CommentTotalCount,
                thumbnailUrl, author, List.of(pickOption3, pickOption4), List.of());
        pickRepository.saveAll(List.of(pick1, pick2, pick3, pick4, pick5, pick6, pick7));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2, pickOption3, pickOption4));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Pick> picks = pickRepository.findPicksByCursor(pageable, Long.MAX_VALUE, null);

        // then
        assertThat(picks).hasSize(7);
    }

    @Test
    @DisplayName("조회수 내림차순으로 Pick을 조회한다.")
    void findPicksByCursorOrderByViewCountDesc() {
        // given
        Pick pick1 = createPickViewTotalCount(new Title("픽1타이틀"), new Count(1));
        Pick pick2 = createPickViewTotalCount(new Title("픽2타이틀"), new Count(2));
        Pick pick3 = createPickViewTotalCount(new Title("픽3타이틀"), new Count(3));
        Pick pick4 = createPickViewTotalCount(new Title("픽4타이틀"), new Count(3));
        Pick pick5 = createPickViewTotalCount(new Title("픽5타이틀"), new Count(4));
        Pick pick6 = createPickViewTotalCount(new Title("픽6타이틀"), new Count(5));

        pickRepository.saveAll(List.of(pick1, pick2, pick3, pick4, pick5, pick6));

        Pageable pageable = PageRequest.of(0, 3);

        // when
        Slice<Pick> picks = pickRepository.findPicksByCursor(pageable, pick4.getId(), PickSort.MOST_VIEWED);

        // then
        assertThat(picks).hasSize(3)
                .extracting(Pick::getTitle)
                .containsExactly(
                        new Title("픽3타이틀"), new Title("픽2타이틀"), new Title("픽1타이틀")
                );
    }

    @Test
    @DisplayName("생성시간 내림차순으로 Pick을 조회한다.")
    void findPicksByCursorOrderByCreatedAtDesc() {
        // given
        Pick pick1 = createPick(new Title("픽1타이틀"));
        Pick pick2 = createPick(new Title("픽2타이틀"));
        Pick pick3 = createPick(new Title("픽3타이틀"));
        Pick pick4 = createPick(new Title("픽4타이틀"));
        Pick pick5 = createPick(new Title("픽5타이틀"));
        Pick pick6 = createPick(new Title("픽6타이틀"));

        pickRepository.saveAll(List.of(pick1, pick2, pick3, pick4, pick5, pick6));

        Pageable pageable = PageRequest.of(0, 3);

        // when
        Slice<Pick> picks = pickRepository.findPicksByCursor(pageable, pick4.getId(), PickSort.LATEST);

        // then
        assertThat(picks).hasSize(3)
                .extracting(Pick::getTitle)
                .containsExactly(
                        new Title("픽3타이틀"), new Title("픽2타이틀"), new Title("픽1타이틀")
                );
    }

    @Test
    @DisplayName("댓글수 내림차순으로 Pick을 조회한다.")
    void findPicksByCursorOrderByDesc() {
        // given
        Pick pick1 = createPickCommentTotalCount(new Title("픽1타이틀"), new Count(1));
        Pick pick2 = createPickCommentTotalCount(new Title("픽2타이틀"), new Count(2));
        Pick pick3 = createPickCommentTotalCount(new Title("픽3타이틀"), new Count(3));
        Pick pick4 = createPickCommentTotalCount(new Title("픽4타이틀"), new Count(3));
        Pick pick5 = createPickCommentTotalCount(new Title("픽5타이틀"), new Count(4));
        Pick pick6 = createPickCommentTotalCount(new Title("픽6타이틀"), new Count(5));

        pickRepository.saveAll(List.of(pick1, pick2, pick3, pick4, pick5, pick6));

        Pageable pageable = PageRequest.of(0, 3);

        // when
        Slice<Pick> picks = pickRepository.findPicksByCursor(pageable, pick4.getId(), PickSort.MOST_COMMENTED);

        // then
        assertThat(picks).hasSize(3)
                .extracting(Pick::getTitle)
                .containsExactly(
                        new Title("픽3타이틀"), new Title("픽2타이틀"), new Title("픽1타이틀")
                );
    }

    @Test
    @DisplayName("인기순으로 Pick을 조회한다."
            + "(현재 가중치 = 댓글수:" + PickPopularScorePolicy.COMMENT_WEIGHT + ", 투표수:" + PickPopularScorePolicy.VOTE_WEIGHT
            + ", 조회수:" + PickPopularScorePolicy.VIEW_WEIGHT + ")")
    void findPicksByCursorOrderByPopular() {
        // given
        Pick pick1 = createPickByPopularScore(new Title("픽1타이틀"), new Count(1));
        Pick pick2 = createPickByPopularScore(new Title("픽2타이틀"), new Count(2));
        Pick pick3 = createPickByPopularScore(new Title("픽3타이틀"), new Count(3));
        Pick pick4 = createPickByPopularScore(new Title("픽4타이틀"), new Count(3));
        Pick pick5 = createPickByPopularScore(new Title("픽5타이틀"), new Count(4));
        Pick pick6 = createPickByPopularScore(new Title("픽6타이틀"), new Count(5));

        pickRepository.saveAll(List.of(pick1, pick2, pick3, pick4, pick5, pick6));

        Pageable pageable = PageRequest.of(0, 3);

        // when
        // pick1이 인기점수가 제일 높다
        Slice<Pick> picks = pickRepository.findPicksByCursor(pageable, pick4.getId(), PickSort.POPULAR);

        // then
        assertThat(picks).hasSize(3)
                .extracting(Pick::getTitle)
                .containsExactly(
                        new Title("픽3타이틀"), new Title("픽2타이틀"), new Title("픽1타이틀")
                );
    }

    @Test
    @DisplayName("findPickWithPickOptionWithPickVoteWithMemberByPickId 쿼리 확인")
    void findPickAndPickOptionAndPickOptionImageById() {
        // given
        Pick pick = createPick(new Title("픽1타이틀"));
        pickRepository.save(pick);

        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽옵션콘텐츠1"), new Count(1),
                pick, PickOptionType.firstPickOption);
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽옵션콘텐츠2"), new Count(2),
                pick, PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        PickOptionImage pickOption1Image1 = cratePickOptionImage("픽옵션1이미지1", pickOption1);
        PickOptionImage pickOption1Image2 = cratePickOptionImage("픽옵션1이미지2", pickOption1);
        PickOptionImage pickOption2Image1 = cratePickOptionImage("픽옵션2이미지1", pickOption2);
        pickOptionImageRepository.saveAll(List.of(pickOption1Image1, pickOption1Image2, pickOption2Image1));

        // when
        Pick findPick = pickRepository.findPickWithPickOptionWithPickVoteWithMemberByPickId(pick.getId()).get();

        // then
        assertThat(findPick.getTitle()).isEqualTo(new Title("픽1타이틀"));

        List<PickOption> findPickOptions = findPick.getPickOptions();
        assertThat(findPickOptions).hasSize(2)
                .extracting("title", "contents", "voteTotalCount")
                .containsExactly(
                        tuple(new Title("픽옵션1"), new PickOptionContents("픽옵션콘텐츠1"), new Count(1)),
                        tuple(new Title("픽옵션2"), new PickOptionContents("픽옵션콘텐츠2"), new Count(2))
                );

        PickOption findPickOption1 = findPickOptions.get(0);
        List<PickOptionImage> findPickOption1Images = findPickOption1.getPickOptionImages();
        assertThat(findPickOption1Images).hasSize(2)
                .extracting("name")
                .containsExactly(
                        "픽옵션1이미지1", "픽옵션1이미지2"
                );

        PickOption findPickOption2 = findPickOptions.get(1);
        List<PickOptionImage> findPickOption2Images = findPickOption2.getPickOptionImages();
        assertThat(findPickOption2Images).hasSize(1)
                .extracting("name")
                .containsExactly(
                        "픽옵션2이미지1"
                );
    }

    private PickOptionImage cratePickOptionImage(String name, PickOption pickOption) {
        PickOptionImage pickOptionImage = PickOptionImage.builder()
                .name(name)
                .imageKey("imageKey")
                .imageUrl("imageUrl")
                .build();

        pickOptionImage.changePickOption(pickOption);

        return pickOptionImage;
    }

    private Pick createPick(Title title) {
        return Pick.builder()
                .title(title)
                .contentStatus(ContentStatus.APPROVAL)
                .build();
    }

    private Pick createPickByPopularScore(Title title, Count popularScore) {
        return Pick.builder()
                .title(title)
                .popularScore(popularScore)
                .contentStatus(ContentStatus.APPROVAL)
                .build();
    }

    private Pick createPickViewTotalCount(Title title, Count viewTotalCount) {
        return Pick.builder()
                .title(title)
                .viewTotalCount(viewTotalCount)
                .contentStatus(ContentStatus.APPROVAL)
                .build();
    }

    private Pick createPickCommentTotalCount(Title title, Count commentTotalCount) {
        return Pick.builder()
                .title(title)
                .commentTotalCount(commentTotalCount)
                .contentStatus(ContentStatus.APPROVAL)
                .build();
    }

    private Pick createPickVoteTotalCount(Title title, Count voteTotalCount) {
        return Pick.builder()
                .title(title)
                .voteTotalCount(voteTotalCount)
                .build();
    }

    private Pick createPick(Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                            Count pickcommentTotalCount, String thumbnailUrl, String author,
                            List<PickOption> pickOptions, List<PickVote> pickVotes
    ) {

        Pick pick = Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteTotalCount)
                .viewTotalCount(pickViewTotalCount)
                .commentTotalCount(pickcommentTotalCount)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .contentStatus(ContentStatus.APPROVAL)
                .build();

        pick.changePickOptions(pickOptions);
        pick.changePickVote(pickVotes);

        return pick;
    }

    private PickOption createPickOption(Title title, PickOptionContents pickOptionContents, Count voteTotalCount,
                                        Pick pick, PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(voteTotalCount)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private PickOption createPickOption(Title title, PickOptionContents pickOptionContents, Count voteTotalCount,
                                        PickOptionType pickOptionType) {
        return PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(voteTotalCount)
                .pickOptionType(pickOptionType)
                .build();
    }
}