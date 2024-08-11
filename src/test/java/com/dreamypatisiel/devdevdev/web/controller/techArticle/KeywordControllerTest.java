package com.dreamypatisiel.devdevdev.web.controller.techArticle;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticKeyword;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticKeywordRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticKeywordService;
import com.dreamypatisiel.devdevdev.web.controller.SupportControllerTest;
import com.dreamypatisiel.devdevdev.web.response.ResultType;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class KeywordControllerTest extends SupportControllerTest {

    @Autowired
    ElasticKeywordService elasticKeywordService;
    @Autowired
    ElasticKeywordRepository elasticKeywordRepository;
    @Autowired
    MemberRepository memberRepository;

    @AfterEach
    void afterEach() {
        elasticKeywordRepository.deleteAll();
    }

    @Test
    @DisplayName("기술블로그 키워드를 검색하면 자동완성 키워드 후보 리스트를 최대 20개 반환한다.")
    void autocompleteKeyword() throws Exception {
        // given
        ElasticKeyword keyword1 = ElasticKeyword.create("자바");
        ElasticKeyword keyword2 = ElasticKeyword.create("자바스크립트");
        ElasticKeyword keyword3 = ElasticKeyword.create("자바가 최고야");
        ElasticKeyword keyword4 = ElasticKeyword.create("스프링");
        ElasticKeyword keyword5 = ElasticKeyword.create("스프링부트");
        List<ElasticKeyword> elasticKeywords = List.of(keyword1, keyword2, keyword3, keyword4, keyword5);
        elasticKeywordRepository.saveAll(elasticKeywords);

        String prefix = "자";

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