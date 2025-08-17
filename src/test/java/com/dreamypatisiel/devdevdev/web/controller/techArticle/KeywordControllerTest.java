package com.dreamypatisiel.devdevdev.web.controller.techArticle;

import com.dreamypatisiel.devdevdev.domain.service.techArticle.keyword.TechKeywordService;
import com.dreamypatisiel.devdevdev.web.controller.SupportControllerTest;
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class KeywordControllerTest extends SupportControllerTest {

    @MockBean
    TechKeywordService techKeywordService;

    @Test
    @DisplayName("기술블로그 키워드를 검색하면 자동완성 키워드 후보 리스트를 최대 20개 반환한다.")
    void autocompleteKeyword() throws Exception {
        // given
        String prefix = "자";
        List<String> result = List.of("자바", "자바 스크립트", "자바가 최고야");
        given(techKeywordService.autocompleteKeyword(prefix)).willReturn(result);

        // when // then
        ResultActions actions = mockMvc.perform(get(DEFAULT_PATH_V1 + "/keywords/auto-complete")
                        .queryParam("prefix", prefix)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.datas").isArray())
                .andExpect(jsonPath("$.datas").isNotEmpty());
    }

}