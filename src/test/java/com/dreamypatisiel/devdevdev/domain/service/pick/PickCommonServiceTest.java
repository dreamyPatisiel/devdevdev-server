package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.SimilarPickResponse;
import com.dreamypatisiel.devdevdev.exception.InternalServerException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PickCommonServiceTest {

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

    @Autowired
    PickCommonService pickCommonService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PickRepository pickRepository;

    @Test
    @DisplayName("타겟 픽픽픽을 기준으로 다른 픽픽픽과 유사도를 계산하여 타겟 픽픽픽을 제외한 승인상태인 상위 3개의 픽픽픽을 조회한다.")
    void findTop3SimilarPicks() {
        // given
        // 픽픽픽 작성자 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        Pick targetPick = createPick(new Title("유소영"), new Count(1), new Count(1), member, ContentStatus.APPROVAL,
                List.of(1.0, 1.0, 1.0));
        Pick pick1 = createPick(new Title("유쏘영"), new Count(2), new Count(5), member, ContentStatus.APPROVAL,
                List.of(0.1, 0.2, 0.3));
        Pick pick2 = createPick(new Title("소영쏘"), new Count(3), new Count(4), member, ContentStatus.APPROVAL,
                List.of(0.2, 0.3, 0.4));
        Pick pick3 = createPick(new Title("쏘영쏘"), new Count(4), new Count(3), member, ContentStatus.APPROVAL,
                List.of(0.3, 0.4, 0.5));
        Pick pick4 = createPick(new Title("쏘주쏘"), new Count(5), new Count(2), member, ContentStatus.READY,
                List.of(0.4, 0.5, 0.6));
        Pick pick5 = createPick(new Title("쏘주"), new Count(6), new Count(1), member, ContentStatus.REJECT,
                List.of(0.4, 0.5, 0.6));
        pickRepository.saveAll(List.of(targetPick, pick1, pick2, pick3, pick4, pick5));

        // when
        List<SimilarPickResponse> top3SimilarPicks = pickCommonService.findTop3SimilarPicks(targetPick.getId());

        // then
        assertThat(top3SimilarPicks).hasSize(3)
                .extracting("id", "title", "voteTotalCount", "commentTotalCount", "similarity")
                .containsExactly(
                        tuple(pick3.getId(), pick3.getTitle().getTitle(), pick3.getVoteTotalCount().getCount(),
                                pick3.getCommentTotalCount().getCount(), 0.9797958971132711),
                        tuple(pick2.getId(), pick2.getTitle().getTitle(), pick2.getVoteTotalCount().getCount(),
                                pick2.getCommentTotalCount().getCount(), 0.9649012813540153),
                        tuple(pick1.getId(), pick1.getTitle().getTitle(), pick1.getVoteTotalCount().getCount(),
                                pick1.getCommentTotalCount().getCount(), 0.9258200997725515)
                );
    }

    @Test
    @DisplayName("타겟 픽픽픽을 기준으로 다른 픽픽픽과 유사도를 계산하여 타겟 픽픽픽을 제외한 상위 3개의 픽픽픽을 조회할 때 타겟 픽픽픽이 없으면 예외가 발생한다.")
    void findTop3SimilarPicks_INVALID_NOT_FOUND_PICK_MESSAGE() {
        // given // when // then
        assertThatThrownBy(() -> pickCommonService.findTop3SimilarPicks(0L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"READY", "REJECT"})
    @DisplayName("타겟 픽픽픽을 기준으로 다른 픽픽픽과 유사도를 계산하여 타겟 픽픽픽을 제외한 상위 3개의 픽픽픽을 조회할 때 타겟 픽픽픽이 승인상태가 아니면 예외가 발생한다.")
    void findTop3SimilarPicks_INVALID_NOT_APPROVAL_STATUS_PICK_MESSAGE(String contentStatus) {
        // given
        // 픽픽픽 작성자 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        Pick targetPick = createPick(new Title("유소영"), new Count(1), new Count(1), member,
                ContentStatus.valueOf(contentStatus), List.of(1.0, 1.0, 1.0));
        pickRepository.save(targetPick);

        // when // then
        assertThatThrownBy(() -> pickCommonService.findTop3SimilarPicks(targetPick.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_NOT_APPROVAL_STATUS_PICK_MESSAGE);
    }

    @Test
    @DisplayName("타겟 픽픽픽을 기준으로 다른 픽픽픽과 유사도를 계산하여 타겟 픽픽픽을 제외한 상위 3개의 픽픽픽을 조회할 때 타겟 픽픽픽의 임베딩 값이 존재하지 않으면 예외가 발생한다.")
    void findTop3SimilarPicks_EXTERNAL_SYSTEM_ERROR_MESSAGE() {
        // given
        // 픽픽픽 작성자 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 임베딩 값이 없음
        Pick targetPick = createPick(new Title("유소영"), new Count(1), new Count(1),
                member, ContentStatus.APPROVAL, List.of());
        pickRepository.save(targetPick);

        // when // then
        assertThatThrownBy(() -> pickCommonService.findTop3SimilarPicks(targetPick.getId()))
                .isInstanceOf(InternalServerException.class);
    }

    private Pick createPick(Title title, Count pickVoteCount, Count commentTotalCount, Member member,
                            ContentStatus contentStatus, List<Double> embeddings) {
        return Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteCount)
                .commentTotalCount(commentTotalCount)
                .member(member)
                .contentStatus(contentStatus)
                .embeddings(embeddings)
                .build();
    }

    private SocialMemberDto createSocialDto(String userId, String name, String nickName, String password, String email,
                                            String socialType, String role) {
        return SocialMemberDto.builder()
                .userId(userId)
                .name(name)
                .nickname(nickName)
                .password(password)
                .email(email)
                .socialType(SocialType.valueOf(socialType))
                .role(Role.valueOf(role))
                .build();
    }
}