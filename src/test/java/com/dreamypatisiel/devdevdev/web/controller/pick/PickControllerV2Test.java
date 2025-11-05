package com.dreamypatisiel.devdevdev.web.controller.pick;

import static com.dreamypatisiel.devdevdev.web.dto.response.ResultType.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.pick.GuestPickServiceV2;
import com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickServiceV2;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.web.controller.SupportControllerTest;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
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

class PickControllerV2Test extends SupportControllerTest {

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
                .andExpect(jsonPath("$.data.totalElements").isNumber())
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
        mockMvc.perform(get("/devdevdev/api/v2/picks/{pickId}/similarties", targetPickId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
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

    @Test
    @DisplayName("회원이 픽픽픽 검색을 조회한다.")
    void searchPicksMain() throws Exception {
        // given
        PickMainOptionResponseV2 pickMainOptionResponse1 = PickMainOptionResponseV2.builder()
                .id(1L)
                .title(new Title("필요하지!"))
                .percent(new BigDecimal("50.0"))
                .isPicked(false)
                .content("검색 좋아")
                .thumbnailImageUrl("https://example.com/image1.png")
                .build();

        PickMainOptionResponseV2 pickMainOptionResponse2 = PickMainOptionResponseV2.builder()
                .id(2L)
                .title(new Title("굳이?"))
                .percent(new BigDecimal("50.0"))
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
        mockMvc.perform(get("/devdevdev/api/v2/picks/search")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("pickId", String.valueOf(Long.MAX_VALUE))
                        .queryParam("searchScore", "10")
                        .queryParam("keyword", "검색")
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
                .andExpect(jsonPath("$.data.content.[0].searchScore").isNumber())
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
                .andExpect(jsonPath("$.data.totalElements").isNumber())
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
}

