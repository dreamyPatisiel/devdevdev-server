package com.dreamypatisiel.devdevdev.elastic.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticKeyword;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticKeywordRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ElasticKeywordServiceTest {

    @Autowired
    ElasticKeywordService elasticKeywordService;
    @Autowired
    ElasticKeywordRepository elasticKeywordRepository;

    @AfterEach
    void afterEach() {
        elasticKeywordRepository.deleteAll();
    }

    @Test
    @DisplayName("검색어와 prefix가 일치하는 키워드를 조회한다.")
    void autocompleteKeyword() throws IOException {
        // given
        ElasticKeyword keyword1 = ElasticKeyword.create(List.of("자바", "Java"));
        ElasticKeyword keyword2 = ElasticKeyword.create(List.of("자바스크립트", "JavaScript"));
        ElasticKeyword keyword3 = ElasticKeyword.create(List.of("스프링", "Spring"));
        ElasticKeyword keyword4 = ElasticKeyword.create(List.of("스프링부트", "SpringBoot"));
        ElasticKeyword keyword5 = ElasticKeyword.create(List.of("챗지피티", "ChatGPT"));
        List<ElasticKeyword> elasticKeywords = List.of(keyword1, keyword2, keyword3, keyword4, keyword5);
        elasticKeywordRepository.saveAll(elasticKeywords);

        String prefix = "자바";

        // when
        List<String> keywords = elasticKeywordService.autocompleteKeyword(prefix);

        // then
        assertThat(keywords)
                .hasSize(2)
                .contains("자바", "자바스크립트");
    }

    @Test
    @DisplayName("영어 대소문자 상관없이 키워드를 조회한다.")
    void autocompleteKeywordRegardlessOfAlphaCase() throws IOException {
        // given
        ElasticKeyword keyword1 = ElasticKeyword.create(List.of("자바", "Java"));
        ElasticKeyword keyword2 = ElasticKeyword.create(List.of("자바스크립트", "JavaScript"));
        ElasticKeyword keyword3 = ElasticKeyword.create(List.of("스프링", "Spring"));
        ElasticKeyword keyword4 = ElasticKeyword.create(List.of("스프링부트", "SpringBoot"));
        ElasticKeyword keyword5 = ElasticKeyword.create(List.of("챗지피티", "ChatGPT"));
        List<ElasticKeyword> elasticKeywords = List.of(keyword1, keyword2, keyword3, keyword4, keyword5);
        elasticKeywordRepository.saveAll(elasticKeywords);

        String prefix = "spr";

        // when
        List<String> keywords = elasticKeywordService.autocompleteKeyword(prefix);

        // then
        assertThat(keywords)
                .hasSize(2)
                .contains("Spring", "SpringBoot");
    }

    @Test
    @DisplayName("일치하는 키워드가 없을 경우 빈 리스트를 반환한다.")
    void autocompleteKeywordNotFound() throws IOException {
        // given
        ElasticKeyword keyword1 = ElasticKeyword.create(List.of("자바", "Java"));
        ElasticKeyword keyword2 = ElasticKeyword.create(List.of("자바스크립트", "JavaScript"));
        ElasticKeyword keyword3 = ElasticKeyword.create(List.of("스프링", "Spring"));
        ElasticKeyword keyword4 = ElasticKeyword.create(List.of("스프링부트", "SpringBoot"));
        ElasticKeyword keyword5 = ElasticKeyword.create(List.of("챗지피티", "ChatGPT"));
        List<ElasticKeyword> elasticKeywords = List.of(keyword1, keyword2, keyword3, keyword4, keyword5);
        elasticKeywordRepository.saveAll(elasticKeywords);

        String prefix = "엘라스틱서치";

        // when
        List<String> keywords = elasticKeywordService.autocompleteKeyword(prefix);

        // then
        assertThat(keywords).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {19, 20, 21, 22})
    @DisplayName("검색어와 prefix가 일치하는 키워드를 최대 20개 조회한다.")
    void autocompleteKeywordWithMax20(int n) throws IOException {
        // given
        List<ElasticKeyword> elasticKeywords = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            elasticKeywords.add(ElasticKeyword.create(List.of("키워드" + i)));
        }
        elasticKeywordRepository.saveAll(elasticKeywords);

        String prefix = "키워드";

        // when
        List<String> keywords = elasticKeywordService.autocompleteKeyword(prefix);

        // then
        assertThat(keywords).hasSizeLessThanOrEqualTo(20);
    }
}