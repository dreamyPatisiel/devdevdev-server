package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechKeyword;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechKeywordRepository;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.keyword.TechKeywordService;
import com.dreamypatisiel.devdevdev.global.utils.HangulUtils;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Testcontainers
class TechKeywordServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    TechKeywordService techKeywordService;

    @Autowired
    TechKeywordRepository techKeywordRepository;
    
    @Autowired
    DataSource dataSource;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("devdevdev_test")
            .withUsername("test")
            .withPassword("test")
            .withCommand(
                "--character-set-server=utf8mb4", 
                "--collation-server=utf8mb4_general_ci",
                "--ngram_token_size=1"
            );

    private static boolean indexesCreated = false;

    @BeforeTransaction
    public void initIndexes() throws SQLException {
        if (!indexesCreated) {
            // 인덱스 생성
            createFulltextIndexesWithJDBC();
            indexesCreated = true;

            // 데이터 추가
            TechKeyword keyword1 = createTechKeyword("자바");
            TechKeyword keyword2 = createTechKeyword("자바스크립트");
            TechKeyword keyword3 = createTechKeyword("스프링");
            TechKeyword keyword4 = createTechKeyword("스프링부트");
            TechKeyword keyword5 = createTechKeyword("꿈빛");
            TechKeyword keyword6 = createTechKeyword("꿈빛 나라");
            TechKeyword keyword7 = createTechKeyword("행복한 꿈빛 파티시엘");
            List<TechKeyword> techKeywords = List.of(keyword1, keyword2, keyword3, keyword4, keyword5, keyword6, keyword7);
            techKeywordRepository.saveAll(techKeywords);
        }
    }

    /**
     * JDBC를 사용하여 MySQL fulltext 인덱스를 생성
     */
    private void createFulltextIndexesWithJDBC() throws SQLException {
        Connection connection = null;
        try {
            // 현재 테스트 클래스의 컨테이너에 직접 연결
            connection = DriverManager.getConnection(
                mysql.getJdbcUrl(),
                mysql.getUsername(), 
                mysql.getPassword()
            );
            connection.setAutoCommit(false); // 트랜잭션 시작

            try (Statement statement = connection.createStatement()) {
                try {
                    // 기존 인덱스가 있다면 삭제
                    statement.executeUpdate("DROP INDEX idx__ft__jamo_key ON tech_keyword");
                    statement.executeUpdate("DROP INDEX idx__ft__chosung_key ON tech_keyword");
                } catch (Exception e) {
                    System.out.println("인덱스 없음 (정상): " + e.getMessage());
                }

                // fulltext 인덱스 생성
                statement.executeUpdate("CREATE FULLTEXT INDEX idx__ft__jamo_key ON tech_keyword (jamo_key) WITH PARSER ngram");
                statement.executeUpdate("CREATE FULLTEXT INDEX idx__ft__chosung_key ON tech_keyword (chosung_key) WITH PARSER ngram");

                connection.commit(); // 트랜잭션 커밋
            }
        } finally {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }

    @Test
    @DisplayName("검색어와 prefix가 일치하는 키워드를 조회한다.")
    void autocompleteKeyword() {
        // given
        String prefix = "자바";

        // when
        List<String> keywords = techKeywordService.autocompleteKeyword(prefix);

        // then
        assertThat(keywords)
                .hasSize(2)
                .contains("자바", "자바스크립트");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ㅈ", "자", "잡", "ㅈㅏ", "ㅈㅏㅂ", "ㅈㅏㅂㅏ"})
    @DisplayName("한글 검색어의 경우 자음, 모음을 분리하여 검색할 수 있다.")
    void autocompleteKoreanKeywordBySeparatingConsonantsAndVowels(String prefix) {
        // given // when
        List<String> keywords = techKeywordService.autocompleteKeyword(prefix);

        // then
        assertThat(keywords)
                .hasSize(2)
                .contains("자바", "자바스크립트");
    }

    @Test
    @DisplayName("한글 검색어의 경우 초성검색을 할 수 있다.")
    void autocompleteKoreanKeywordByChosung() {
        // given
        String prefix = "ㅅㅍㄹ";

        // when
        List<String> keywords = techKeywordService.autocompleteKeyword(prefix);

        // then
        assertThat(keywords)
                .hasSize(2)
                .contains("스프링", "스프링부트");
    }

    @Test
    @DisplayName("일치하는 키워드가 없을 경우 빈 리스트를 반환한다.")
    void autocompleteKeywordNotFound() {
        // given
        String prefix = "엘라스틱서치";

        // when
        List<String> keywords = techKeywordService.autocompleteKeyword(prefix);

        // then
        assertThat(keywords).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {19, 20, 21, 22})
    @DisplayName("검색 결과는 최대 20개로 제한된다.")
    void autocompleteKeywordLimitTo20Results(int n) {
        // given
        List<TechKeyword> techKeywords = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            techKeywords.add(createTechKeyword("키워드" + i));
        }
        techKeywordRepository.saveAll(techKeywords);

        // when
        List<String> result = techKeywordService.autocompleteKeyword("키워드");

        // then
        assertThat(result).hasSizeLessThanOrEqualTo(20);
    }

    @Test
    @DisplayName("검색 결과가 관련도 순으로 정렬된다.")
    void autocompleteKeywordSortedByRelevance() {
        // given // when
        List<String> result = techKeywordService.autocompleteKeyword("꿈빛");

        // then
        assertThat(result).isNotEmpty();
        // 더 정확히 매치되는 "꿈빛"이 상위에 나와야 한다
        assertThat(result.get(0)).isEqualTo("꿈빛");
    }

    private TechKeyword createTechKeyword(String keyword) {
        return TechKeyword.builder()
                .keyword(keyword)
                .jamoKey(HangulUtils.convertToJamo(keyword))
                .chosungKey(HangulUtils.extractChosung(keyword))
                .build();
    }
}