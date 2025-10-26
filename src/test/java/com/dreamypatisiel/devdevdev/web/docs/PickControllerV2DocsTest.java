package com.dreamypatisiel.devdevdev.web.docs;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.pick.PickServiceStrategy;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType.firstPickOption;
import static com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType.secondPickOption;
import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.authenticationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.pickSortType;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PickControllerV2DocsTest extends SupportControllerDocsTest {

    @Autowired
    PickRepository pickRepository;
    @Autowired
    PickOptionRepository pickOptionRepository;
    @Autowired
    PickOptionImageRepository pickOptionImageRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PickPopularScorePolicy pickPopularScorePolicy;
    @Autowired
    PickServiceStrategy pickServiceStrategy;

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

        // 픽픽픽 옵션 이미지 생성
        PickOptionImage firstPickOptionImage = createPickOptionImage("이미지1", "http://iamge1.png", pickOption1);
        PickOptionImage secondPickOptionImage = createPickOptionImage("이미지2", "http://iamge2.png", pickOption2);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));


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
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v2/picks")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("pickId", String.valueOf(Long.MAX_VALUE))
                        .queryParam("pickSort", PickSort.LATEST.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("pick-main-v2",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰"),
                        headerWithName("Anonymous-Member-Id").optional().description("익명 회원 아이디")
                ),
                queryParameters(
                        parameterWithName("pickId").optional().description("픽픽픽 아이디"),
                        parameterWithName("pickSort").optional().description("픽픽픽 정렬 조건").attributes(pickSortType()),
                        parameterWithName("size").optional().description("조회되는 데이터 수")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),

                        fieldWithPath("data.content").type(ARRAY).description("픽픽픽 메인 배열"),
                        fieldWithPath("data.content[].id").type(NUMBER).description("픽픽픽 아이디"),
                        fieldWithPath("data.content[].title").type(STRING).description("픽픽픽 제목"),
                        fieldWithPath("data.content[].voteTotalCount").type(NUMBER).description("픽픽픽 전체 투표 수"),
                        fieldWithPath("data.content[].commentTotalCount").type(NUMBER).description("픽픽픽 전체 댓글 수"),
                        fieldWithPath("data.content[].viewTotalCount").type(NUMBER).description("픽픽픽 조회 수"),
                        fieldWithPath("data.content[].popularScore").type(NUMBER).description("픽픽픽 인기점수"),
                        fieldWithPath("data.content[].isVoted").attributes(authenticationType()).type(BOOLEAN)
                                .description("픽픽픽 투표 여부(익명 사용자는 필드가 없다.)"),
                        fieldWithPath("data.content[].isNew").attributes(authenticationType()).type(BOOLEAN)
                                .description("일주일 이내 게시글 여부 (NEW)"),

                        fieldWithPath("data.content[].pickOptions").type(ARRAY).description("픽픽픽 옵션 배열"),
                        fieldWithPath("data.content[].pickOptions[].id").type(NUMBER).description("픽픽픽 옵션 아이디"),
                        fieldWithPath("data.content[].pickOptions[].title").type(STRING).description("픽픽픽 옵션 제목"),
                        fieldWithPath("data.content[].pickOptions[].percent").type(NUMBER).description("픽픽픽 옵션 투표율(%)"),
                        fieldWithPath("data.content[].pickOptions[].content").type(STRING).description("픽픽픽 옵션 내용 (NEW)"),
                        fieldWithPath("data.content[].pickOptions[].thumbnailImageUrl").type(STRING).description("픽픽픽 썸네일 이미지 (NEW)"),
                        fieldWithPath("data.content[].pickOptions[].isPicked").attributes(authenticationType()).type(
                                BOOLEAN).description("픽픽픽 옵션 투표 여부(익명 사용자는 필드가 없다.)"),
                        fieldWithPath("data.content[].pickOptions[].id").type(NUMBER).description("픽픽픽 옵션 아이디"),
                        fieldWithPath("data.content[].pickOptions[].title").type(STRING).description("픽픽픽 옵션 제목"),
                        fieldWithPath("data.content[].pickOptions[].percent").type(NUMBER).description("픽픽픽 옵션 투표율(%)"),
                        fieldWithPath("data.content[].pickOptions[].content").type(STRING).description("픽픽픽 옵션 내용 (NEW)"),
                        fieldWithPath("data.content[].pickOptions[].thumbnailImageUrl").type(STRING).description("픽픽픽 썸네일 이미지 (NEW)"),
                        fieldWithPath("data.content[].pickOptions[].isPicked").attributes(authenticationType()).type(
                                BOOLEAN).description("픽픽픽 옵션 투표 여부(익명 사용자는 필드가 없다.)"),

                        fieldWithPath("data.pageable").type(OBJECT).description("픽픽픽 메인 페이지네이션 정보"),
                        fieldWithPath("data.pageable.pageNumber").type(NUMBER).description("페이지 번호"),
                        fieldWithPath("data.pageable.pageSize").type(NUMBER).description("페이지 사이즈"),

                        fieldWithPath("data.pageable.sort").type(OBJECT).description("정렬 정보"),
                        fieldWithPath("data.pageable.sort.empty").type(BOOLEAN).description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.pageable.sort.sorted").type(BOOLEAN).description("정렬 여부"),
                        fieldWithPath("data.pageable.sort.unsorted").type(BOOLEAN).description("비정렬 여부"),

                        fieldWithPath("data.pageable.offset").type(NUMBER).description("페이지 오프셋 (페이지 크기 * 페이지 번호)"),
                        fieldWithPath("data.pageable.paged").type(BOOLEAN).description("페이지 정보 포함 여부"),
                        fieldWithPath("data.pageable.unpaged").type(BOOLEAN).description("페이지 정보 비포함 여부"),

                        fieldWithPath("data.first").type(BOOLEAN).description("현재 페이지가 첫 페이지 여부"),
                        fieldWithPath("data.last").type(BOOLEAN).description("현재 페이지가 마지막 페이지 여부"),
                        fieldWithPath("data.size").type(NUMBER).description("페이지 크기"),
                        fieldWithPath("data.number").type(NUMBER).description("현재 페이지"),

                        fieldWithPath("data.sort").type(OBJECT).description("정렬 정보"),
                        fieldWithPath("data.sort.empty").type(BOOLEAN).description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.sort.sorted").type(BOOLEAN).description("정렬 상태 여부"),
                        fieldWithPath("data.sort.unsorted").type(BOOLEAN).description("비정렬 상태 여부"),
                        fieldWithPath("data.numberOfElements").type(NUMBER).description("현재 페이지 데이터 수"),
                        fieldWithPath("data.totalElements").type(NUMBER).description("전체 픽픽픽 데이터 수 (NEW)"),
                        fieldWithPath("data.empty").type(BOOLEAN).description("현재 빈 페이지 여부")
                )
        ));
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
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v2/picks/{pickId}/similarties", targetPick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("pick-similarity-v2",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디")
                ),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰"),
                        headerWithName("Anonymous-Member-Id").optional().description("익명 회원 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("datas").type(ARRAY).description("나도 고민했는데 픽픽픽 배열"),
                        fieldWithPath("datas[].id").type(NUMBER).description("픽픽픽 아이디"),
                        fieldWithPath("datas[].title").type(STRING).description("픽픽픽 제목"),
                        fieldWithPath("datas[].voteTotalCount").type(NUMBER).description("픽픽픽 전체 투표 수"),
                        fieldWithPath("datas[].commentTotalCount").type(NUMBER).description("픽픽픽 전체 댓글 수"),
                        fieldWithPath("datas[].similarity").type(NUMBER).description("픽픽픽 유사도"),
                        fieldWithPath("datas[].isNew").type(BOOLEAN).description("일주일 이내 게시글 여부 (NEW)")
                )
        ));
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

    private PickOptionImage createPickOptionImage(String name, String imageUrl, PickOption pickOption) {
        PickOptionImage pickOptionImage = PickOptionImage.builder()
                .name(name)
                .imageUrl(imageUrl)
                .imageKey("imageKey")
                .build();

        pickOptionImage.changePickOption(pickOption);

        return pickOptionImage;
    }
}

