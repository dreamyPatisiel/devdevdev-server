package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.authenticationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.pickSortType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.pick.GuestPickServiceV2;
import com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickServiceV2;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.*;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainOptionResponseV2;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainResponseV2;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainSearchResponseV2;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.SimilarPickResponseV2;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.authenticationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.pickSortType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.yearMonthDateTimeType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

class PickControllerV2DocsTest extends SupportControllerDocsTest {

    @MockBean
    GuestPickServiceV2 guestPickServiceV2;
    @MockBean
    MemberPickServiceV2 memberPickServiceV2;

    @Test
    @DisplayName("회원이 픽픽픽 메인을 조회한다.")
    void getPicksMainByMember() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // Mock 데이터 생성
        PickMainOptionResponseV2 option1 = PickMainOptionResponseV2.builder()
                .id(1L)
                .title(new Title("픽옵션1"))
                .percent(new java.math.BigDecimal("50"))
                .isPicked(true)
                .content("픽콘텐츠1")
                .thumbnailImageUrl("http://image1.png")
                .build();

        PickMainOptionResponseV2 option2 = PickMainOptionResponseV2.builder()
                .id(2L)
                .title(new Title("픽옵션2"))
                .percent(new java.math.BigDecimal("50"))
                .isPicked(false)
                .content("픽콘텐츠2")
                .thumbnailImageUrl("http://image2.png")
                .build();

        PickMainResponseV2 pickMainResponse = PickMainResponseV2.builder()
                .id(1L)
                .title(new Title("픽1타이틀"))
                .voteTotalCount(new Count(2))
                .commentTotalCount(new Count(2))
                .viewTotalCount(new Count(2))
                .popularScore(new Count(100))
                .isVoted(true)
                .isNew(true)
                .pickOptions(List.of(option1, option2))
                .build();

        Slice<PickMainResponseV2> result = new SliceCustom<>(List.of(pickMainResponse), pageable, false, 1L);
        when(memberPickServiceV2.findPicksMain(any(Pageable.class), any(), any(),
                any(), any(Authentication.class))).thenReturn(result);

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
                        fieldWithPath("data.content[].pickOptions[].thumbnailImageUrl").type(STRING)
                                .description("픽픽픽 썸네일 이미지 (NEW)"),
                        fieldWithPath("data.content[].pickOptions[].isPicked").attributes(authenticationType()).type(
                                BOOLEAN).description("픽픽픽 옵션 투표 여부(익명 사용자는 필드가 없다.)"),
                        fieldWithPath("data.content[].pickOptions[].id").type(NUMBER).description("픽픽픽 옵션 아이디"),
                        fieldWithPath("data.content[].pickOptions[].title").type(STRING).description("픽픽픽 옵션 제목"),
                        fieldWithPath("data.content[].pickOptions[].percent").type(NUMBER).description("픽픽픽 옵션 투표율(%)"),
                        fieldWithPath("data.content[].pickOptions[].content").type(STRING).description("픽픽픽 옵션 내용 (NEW)"),
                        fieldWithPath("data.content[].pickOptions[].thumbnailImageUrl").type(STRING)
                                .description("픽픽픽 썸네일 이미지 (NEW)"),
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
        Long targetPickId = 1L;

        // Mock 데이터 생성
        SimilarPickResponseV2 similarPick1 = SimilarPickResponseV2.builder()
                .id(2L)
                .title("유쏘영")
                .voteTotalCount(2L)
                .commentTotalCount(5L)
                .similarity(0.95)
                .isNew(true)
                .build();

        SimilarPickResponseV2 similarPick2 = SimilarPickResponseV2.builder()
                .id(3L)
                .title("소영쏘")
                .voteTotalCount(3L)
                .commentTotalCount(4L)
                .similarity(0.85)
                .isNew(false)
                .build();

        SimilarPickResponseV2 similarPick3 = SimilarPickResponseV2.builder()
                .id(4L)
                .title("쏘영쏘")
                .voteTotalCount(4L)
                .commentTotalCount(3L)
                .similarity(0.75)
                .isNew(false)
                .build();

        List<SimilarPickResponseV2> result = List.of(similarPick1, similarPick2, similarPick3);
        when(memberPickServiceV2.findTop3SimilarPicksV2(anyLong())).thenReturn(result);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v2/picks/{pickId}/similarties", targetPickId)
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

    @Test
    @DisplayName("회원이 픽픽픽 상세를 조회한다. V2")
    void getPickDetail() throws Exception {
        // given
        Long pickId = 1L;

        // Mock 데이터 생성
        PickDetailOptionImageResponse image1 = PickDetailOptionImageResponse.builder()
                .id(1L)
                .imageUrl("http://image1.png")
                .build();

        PickDetailOptionImageResponse image2 = PickDetailOptionImageResponse.builder()
                .id(2L)
                .imageUrl("http://image2.png")
                .build();

        PickDetailOptionResponse firstOption = PickDetailOptionResponse.builder()
                .id(1L)
                .title("픽옵션1")
                .isPicked(true)
                .percent(new BigDecimal("60"))
                .content("픽콘텐츠1")
                .voteTotalCount(3L)
                .pickDetailOptionImagesResponse(List.of(image1))
                .build();

        PickDetailOptionResponse secondOption = PickDetailOptionResponse.builder()
                .id(2L)
                .title("픽옵션2")
                .isPicked(false)
                .percent(new BigDecimal("40"))
                .content("픽콘텐츠2")
                .voteTotalCount(2L)
                .pickDetailOptionImagesResponse(List.of(image2))
                .build();

        Map<PickOptionType, PickDetailOptionResponse> pickOptions = Map.of(
                PickOptionType.firstPickOption, firstOption,
                PickOptionType.secondPickOption, secondOption
        );

        PickDetailResponseV2 response = PickDetailResponseV2.builder()
                .userId("dre***@gmail.com")
                .nickname("꿈빛파티시엘")
                .pickCreatedAt(LocalDateTime.of(2024, 1, 1, 12, 0, 0))
                .pickTitle("픽픽픽 제목")
                .voteTotalCount(5L)
                .commentTotalCount(10L)
                .isAuthor(true)
                .isVoted(true)
                .pickOptions(pickOptions)
                .build();

        when(memberPickServiceV2.findPickDetail(anyLong(), any(), any(Authentication.class)))
                .thenReturn(response);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v2/picks/{pickId}", pickId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("pick-detail-v2",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰"),
                        headerWithName("Anonymous-Member-Id").optional().description("익명 회원 아이디")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),

                        fieldWithPath("data.nickname").type(STRING).description("픽픽픽 작성자 닉네임"),
                        fieldWithPath("data.userId").type(STRING).description("픽픽픽 작성자 아이디"),
                        fieldWithPath("data.pickCreatedAt").type(STRING).description("픽픽픽 생성 일시")
                                .attributes(yearMonthDateTimeType()),
                        fieldWithPath("data.pickTitle").type(STRING).description("픽픽픽 제목"),
                        fieldWithPath("data.voteTotalCount").type(NUMBER).description("픽픽픽 전체 투표 수 (NEW)"),
                        fieldWithPath("data.commentTotalCount").type(NUMBER).description("픽픽픽 전체 댓글 수 (NEW)"),
                        fieldWithPath("data.isAuthor").type(BOOLEAN).description("현재 로그인한 회원이 픽픽픽 작성자 여부"),
                        fieldWithPath("data.isVoted").type(BOOLEAN).description("픽픽픽 투표 여부"),

                        fieldWithPath("data.pickOptions").type(OBJECT).description("픽픽픽 옵션 객체"),

                        fieldWithPath("data.pickOptions.firstPickOption").type(OBJECT).description("픽픽픽 첫번째 옵션 객체"),
                        fieldWithPath("data.pickOptions.firstPickOption.id").type(NUMBER)
                                .description("첫 번째 픽픽픽 옵션 아이디"),
                        fieldWithPath("data.pickOptions.firstPickOption.title").type(STRING)
                                .description("첫 번째 픽픽픽 옵션 제목"),
                        fieldWithPath("data.pickOptions.firstPickOption.isPicked").type(BOOLEAN)
                                .description("첫 번째 픽픽픽 옵션 투표 여부"),
                        fieldWithPath("data.pickOptions.firstPickOption.percent").type(NUMBER)
                                .description("첫 번째 픽픽픽 옵션 득표율(%)"),
                        fieldWithPath("data.pickOptions.firstPickOption.content").type(STRING)
                                .description("첫 번째 픽픽픽 옵션 내용"),
                        fieldWithPath("data.pickOptions.firstPickOption.voteTotalCount").type(NUMBER)
                                .description("첫 번째 픽픽픽 옵션 득표수"),
                        fieldWithPath("data.pickOptions.firstPickOption.pickDetailOptionImages").type(ARRAY)
                                .description("첫 번째 픽픽픽 옵션 이미지 배열"),
                        fieldWithPath("data.pickOptions.firstPickOption.pickDetailOptionImages.[].id").type(NUMBER)
                                .description("첫 번째 픽픽픽 옵션 이미지 아이디"),
                        fieldWithPath("data.pickOptions.firstPickOption.pickDetailOptionImages.[].imageUrl").type(
                                STRING).description("첫 번째 픽픽픽 옵션 이미지 url"),

                        fieldWithPath("data.pickOptions.secondPickOption").type(OBJECT).description("픽픽픽 두번째 옵션 객체"),
                        fieldWithPath("data.pickOptions.secondPickOption.id").type(NUMBER)
                                .description("두 번째 픽픽픽 옵션 아이디"),
                        fieldWithPath("data.pickOptions.secondPickOption.title").type(STRING)
                                .description("두 번째 픽픽픽 옵션 제목"),
                        fieldWithPath("data.pickOptions.secondPickOption.isPicked").type(BOOLEAN)
                                .description("두 번째 픽픽픽 옵션 투표 여부"),
                        fieldWithPath("data.pickOptions.secondPickOption.percent").type(NUMBER)
                                .description("두 번째 픽픽픽 옵션 득표율(%)"),
                        fieldWithPath("data.pickOptions.secondPickOption.content").type(STRING)
                                .description("두 번째 픽픽픽 옵션 내용"),
                        fieldWithPath("data.pickOptions.secondPickOption.voteTotalCount").type(NUMBER)
                                .description("두 번째 픽픽픽 옵션 득표수"),
                        fieldWithPath("data.pickOptions.secondPickOption.pickDetailOptionImages").type(ARRAY)
                                .description("두 번째 픽픽픽 옵션 이미지 배열"),
                        fieldWithPath("data.pickOptions.secondPickOption.pickDetailOptionImages.[].id").type(NUMBER)
                                .description("두 번째 픽픽픽 옵션 이미지 아이디"),
                        fieldWithPath("data.pickOptions.secondPickOption.pickDetailOptionImages.[].imageUrl").type(
                                STRING).description("두 번째 픽픽픽 옵션 이미지 url")
                )
        ));
    }

    @Test
    @DisplayName("회원이 픽픽픽 검색을 조회한다.")
    void searchPicksMain() throws Exception {
        // given
        PickMainOptionResponseV2 pickMainOptionResponse1 = PickMainOptionResponseV2.builder()
                .id(1L)
                .title(new Title("필요하지!"))
                .percent(new BigDecimal("49.0"))
                .isPicked(false)
                .content("검색 좋아")
                .thumbnailImageUrl("https://example.com/image1.png")
                .build();

        PickMainOptionResponseV2 pickMainOptionResponse2 = PickMainOptionResponseV2.builder()
                .id(2L)
                .title(new Title("굳이?"))
                .percent(new BigDecimal("51.0"))
                .isPicked(true)
                .content("검색할 일이 있을까?")
                .thumbnailImageUrl("https://example.com/image2.png")
                .build();

        PickMainSearchResponseV2 pickMainSearchResponseV2 = PickMainSearchResponseV2.searchBuilder()
                .id(1L)
                .title(new Title("검색기능 필요해?"))
                .voteTotalCount(new Count(100_000L))
                .commentTotalCount(new Count(99_109L))
                .viewTotalCount(new Count(81_229L))
                .popularScore(new Count(1000))
                .pickOptions(List.of(pickMainOptionResponse1, pickMainOptionResponse2))
                .isVoted(true)
                .isNew(true)
                .searchScore(60.0)
                .build();

        Pageable pageable = PageRequest.of(0, 10);

        SliceCustom<PickMainSearchResponseV2> response = new SliceCustom<>(List.of(pickMainSearchResponseV2),
                pageable, 1L);

        // when
        when(memberPickServiceV2.findPickMainSearch(any(), any(), any(), any(), any(), any())).thenReturn(response);

        // then
        ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.get("/devdevdev/api/v2/picks/search")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("pickId", String.valueOf(Long.MAX_VALUE))
                        .queryParam("searchScore", "10")
                        .queryParam("keyword", "검색")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("pick-search-v2",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰"),
                        headerWithName("Anonymous-Member-Id").optional().description("익명 회원 아이디")
                ),
                queryParameters(
                        parameterWithName("pickId").optional().description("픽픽픽 아이디"),
                        parameterWithName("keyword").optional().description("픽픽픽 검색어"),
                        parameterWithName("searchScore").optional().description("게시글 검색 점수"),
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
                        fieldWithPath("data.content[].searchScore").type(NUMBER).description("검색 결과 점수"),

                        fieldWithPath("data.content[].pickOptions").type(ARRAY).description("픽픽픽 옵션 배열"),
                        fieldWithPath("data.content[].pickOptions[].id").type(NUMBER).description("픽픽픽 옵션 아이디"),
                        fieldWithPath("data.content[].pickOptions[].title").type(STRING).description("픽픽픽 옵션 제목"),
                        fieldWithPath("data.content[].pickOptions[].percent").type(NUMBER).description("픽픽픽 옵션 투표율(%)"),
                        fieldWithPath("data.content[].pickOptions[].content").type(STRING).description("픽픽픽 옵션 내용 (NEW)"),
                        fieldWithPath("data.content[].pickOptions[].thumbnailImageUrl").type(STRING)
                                .description("픽픽픽 썸네일 이미지 (NEW)"),
                        fieldWithPath("data.content[].pickOptions[].isPicked").attributes(authenticationType()).type(
                                BOOLEAN).description("픽픽픽 옵션 투표 여부(익명 사용자는 필드가 없다.)"),
                        fieldWithPath("data.content[].pickOptions[].id").type(NUMBER).description("픽픽픽 옵션 아이디"),
                        fieldWithPath("data.content[].pickOptions[].title").type(STRING).description("픽픽픽 옵션 제목"),
                        fieldWithPath("data.content[].pickOptions[].percent").type(NUMBER).description("픽픽픽 옵션 투표율(%)"),
                        fieldWithPath("data.content[].pickOptions[].content").type(STRING).description("픽픽픽 옵션 내용 (NEW)"),
                        fieldWithPath("data.content[].pickOptions[].thumbnailImageUrl").type(STRING)
                                .description("픽픽픽 썸네일 이미지 (NEW)"),
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
}

