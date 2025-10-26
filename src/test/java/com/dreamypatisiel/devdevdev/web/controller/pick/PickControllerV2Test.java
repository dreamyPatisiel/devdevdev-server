package com.dreamypatisiel.devdevdev.web.controller.pick;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.pick.PickServiceStrategy;
import com.dreamypatisiel.devdevdev.domain.service.pick.PickServiceV2;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.controller.ApiVersion;
import com.dreamypatisiel.devdevdev.web.controller.SupportControllerTest;
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainResponseV2;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.SimilarPickResponseV2;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType.firstPickOption;
import static com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType.secondPickOption;
import static com.dreamypatisiel.devdevdev.web.dto.response.ResultType.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PickControllerV2Test extends SupportControllerTest {

    @Autowired
    PickRepository pickRepository;
    @Autowired
    PickOptionRepository pickOptionRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PickPopularScorePolicy pickPopularScorePolicy;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("회원이 픽픽픽 메인을 조회한다.")
    void getPicksMainByMember() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"), new Count(1),
                firstPickOption);
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"), new Count(1),
                secondPickOption);

        Title title = new Title("픽1타이틀");
        Count count = new Count(2);
        String thumbnailUrl = "https://devdevdev.co.kr/devdevdev/api/v1/pick/image/1";
        String author = "운영자";
        Pick pick = createPick(member, title, count, count, count, thumbnailUrl, author, ContentStatus.APPROVAL,
                List.of(pickOption1, pickOption2), List.of());
        pick.changePopularScore(pickPopularScorePolicy);

        pickRepository.save(pick);
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        Pageable pageable = PageRequest.of(0, 10);

        // when // then
        mockMvc.perform(get("/devdevdev/api/v2/picks")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("pickId", String.valueOf(Long.MAX_VALUE))
                        .queryParam("pickSort", PickSort.LATEST.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].title").isString())
                .andExpect(jsonPath("$.data.content.[0].voteTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].commentTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].viewTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].popularScore").isNumber())
                .andExpect(jsonPath("$.data.content.[0].isVoted").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].pickOptions").isArray())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[0].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[0].title").isString())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[0].percent").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[0].isPicked").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[1].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[1].title").isString())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[1].percent").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[1].isPicked").isBoolean())
                .andExpect(jsonPath("$.data.pageable").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable.pageNumber").isNumber())
                .andExpect(jsonPath("$.data.pageable.pageSize").isNumber())
                .andExpect(jsonPath("$.data.pageable.sort").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable.sort.empty").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.sorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.unsorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.offset").isNumber())
                .andExpect(jsonPath("$.data.pageable.paged").isBoolean())
                .andExpect(jsonPath("$.data.pageable.unpaged").isBoolean())
                .andExpect(jsonPath("$.data.first").isBoolean())
                .andExpect(jsonPath("$.data.last").isBoolean())
                .andExpect(jsonPath("$.data.size").isNumber())
                .andExpect(jsonPath("$.data.number").isNumber())
                .andExpect(jsonPath("$.data.sort").isNotEmpty())
                .andExpect(jsonPath("$.data.sort.empty").isBoolean())
                .andExpect(jsonPath("$.data.sort.sorted").isBoolean())
                .andExpect(jsonPath("$.data.sort.unsorted").isBoolean())
                .andExpect(jsonPath("$.data.numberOfElements").isNumber())
                .andExpect(jsonPath("$.data.empty").isBoolean());
    }

    @Test
    @DisplayName("나도 고민했는데 픽픽픽을 조회한다.")
    void getSimilarPicks() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
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

        // when // then
        mockMvc.perform(get("/devdevdev/api/v2/picks/{pickId}/similarties", targetPick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.datas").isArray())
                .andExpect(jsonPath("$.datas.[0].id").isNumber())
                .andExpect(jsonPath("$.datas.[0].title").isString())
                .andExpect(jsonPath("$.datas.[0].voteTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[0].commentTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[0].similarity").isNumber())
                .andExpect(jsonPath("$.datas.[0].isNew").isBoolean())
                .andExpect(jsonPath("$.datas.[1].id").isNumber())
                .andExpect(jsonPath("$.datas.[1].title").isString())
                .andExpect(jsonPath("$.datas.[1].voteTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[1].commentTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[1].similarity").isNumber())
                .andExpect(jsonPath("$.datas.[1].isNew").isBoolean())
                .andExpect(jsonPath("$.datas.[2].id").isNumber())
                .andExpect(jsonPath("$.datas.[2].title").isString())
                .andExpect(jsonPath("$.datas.[2].voteTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[2].commentTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[2].similarity").isNumber())
                .andExpect(jsonPath("$.datas.[2].isNew").isBoolean());
    }

    private Pick createPick(Member member, Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                            Count pickCommentTotalCount, String thumbnailUrl, String author,
                            ContentStatus contentStatus,
                            List<PickOption> pickOptions, List<com.dreamypatisiel.devdevdev.domain.entity.PickVote> pickVotes
    ) {

        Pick pick = Pick.builder()
                .member(member)
                .title(title)
                .voteTotalCount(pickVoteTotalCount)
                .viewTotalCount(pickViewTotalCount)
                .commentTotalCount(pickCommentTotalCount)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .contentStatus(contentStatus)
                .build();

        pick.changePickOptions(pickOptions);
        pick.changePickVote(pickVotes);

        return pick;
    }

    private PickOption createPickOption(Title title, PickOptionContents pickOptionContents, Count voteTotalCount,
                                        com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType pickOptionType) {
        return PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(voteTotalCount)
                .pickOptionType(pickOptionType)
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
}

