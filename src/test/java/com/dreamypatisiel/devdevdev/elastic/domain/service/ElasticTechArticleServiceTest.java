//package com.dreamypatisiel.devdevdev.elastic.domain.service;
//
//import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_CURSOR_SCORE_MESSAGE;
//import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatCode;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
//import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
//import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
//import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
//import com.dreamypatisiel.devdevdev.exception.ElasticTechArticleException;
//import com.dreamypatisiel.devdevdev.exception.NotFoundException;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.ValueSource;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.elasticsearch.UncategorizedElasticsearchException;
//import org.springframework.data.elasticsearch.core.SearchHit;
//import org.springframework.data.elasticsearch.core.SearchHits;
//
//public class ElasticTechArticleServiceTest extends ElasticsearchSupportTest {
//
//    @Autowired
//    ElasticTechArticleService elasticTechArticleService;
//    @Autowired
//    TechArticleRepository techArticleRepository;
//    @Autowired
//    ElasticTechArticleRepository elasticTechArticleRepository;
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그 메인을 조회한다. (기본정렬은 최신순)")
//    void getTechArticles() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null, null,
//                null, null, null);
//        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream().map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(elasticTechArticles)
//                .hasSize(pageable.getPageSize())
//                .extracting(ElasticTechArticle::getRegDate)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그 메인을 최신순으로 조회한다.")
//    void getTechArticlesOrderByLATEST() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null,
//                TechArticleSort.LATEST, null, null, null);
//        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(elasticTechArticles)
//                .hasSize(pageable.getPageSize())
//                .extracting(ElasticTechArticle::getRegDate)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그 메인을 조회수 내림차순으로 조회한다.")
//    void getTechArticlesOrderByMOST_VIEWED() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null,
//                TechArticleSort.MOST_VIEWED, null, null, null);
//        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream().map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(elasticTechArticles)
//                .hasSize(pageable.getPageSize())
//                .extracting(ElasticTechArticle::getViewTotalCount)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그 메인을 댓글수 내림차순으로 조회한다.")
//    void getTechArticlesOrderByMOST_COMMENTED() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null,
//                TechArticleSort.MOST_COMMENTED, null, null, null);
//        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream().map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(elasticTechArticles)
//                .hasSize(pageable.getPageSize())
//                .extracting(ElasticTechArticle::getCommentTotalCount)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그 메인을 인기점수 내림차순으로 조회한다.")
//    void getTechArticlesOrderByPOPULAR_SCORE() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null,
//                TechArticleSort.POPULAR, null, null, null);
//        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream().map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(elasticTechArticles)
//                .hasSize(pageable.getPageSize())
//                .extracting(ElasticTechArticle::getPopularScore)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그 메인을 정확도순으로 조회하면 최신순으로 조회된다.")
//    void getTechArticlesOrderByHIGHEST_SCORE() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null,
//                TechArticleSort.HIGHEST_SCORE, null, null, null);
//        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream().map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(elasticTechArticles)
//                .hasSize(pageable.getPageSize())
//                .extracting(ElasticTechArticle::getRegDate)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그 메인을 조회할 때, " +
//            "존재하지 않는 엘라스틱 기술블로그 ID라면 예외가 발생한다.")
//    void getTechArticlesWithCursorException() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when // then
//        assertThatThrownBy(
//                () -> elasticTechArticleService.getTechArticles(pageable, "dontExistElasticId", null, null, null, null))
//                .isInstanceOf(NotFoundException.class)
//                .hasMessage(NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE);
//    }
//
//    @Test
//    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그 메인을 최신순으로 조회한다.")
//    void getTechArticlesWithCursorOrderByLATEST() {
//        // given
//        Pageable prevPageable = PageRequest.of(0, 1);
//        Pageable pageable = PageRequest.of(0, 10);
//
//        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.getTechArticles(
//                prevPageable, null,
//                TechArticleSort.LATEST, null, null, null);
//        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//        ElasticTechArticle cursor = elasticTechArticles1.getLast();
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles2 = elasticTechArticleService.getTechArticles(pageable,
//                cursor.getId(), TechArticleSort.LATEST, null, null, null);
//        List<ElasticTechArticle> elasticTechArticles2 = techArticles2.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(elasticTechArticles2)
//                .hasSize(pageable.getPageSize())
//                .extracting(ElasticTechArticle::getRegDate)
//                .isSortedAccordingTo(Comparator.reverseOrder())
//                .allMatch(date -> !date.isAfter(cursor.getRegDate()));
//    }
//
//    @Test
//    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그 메인을 조회수 내림차순으로 조회한다.")
//    void getTechArticlesWithCursorOrderByMOST_VIEWED() {
//        // given
//        Pageable prevPageable = PageRequest.of(0, 1);
//        Pageable pageable = PageRequest.of(0, 10);
//
//        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.getTechArticles(
//                prevPageable, null,
//                TechArticleSort.MOST_VIEWED, null, null, null);
//        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//        ElasticTechArticle cursor = elasticTechArticles1.getLast();
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles2 = elasticTechArticleService.getTechArticles(pageable,
//                cursor.getId(), TechArticleSort.MOST_VIEWED, null, null, null);
//        List<ElasticTechArticle> elasticTechArticles2 = techArticles2.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(elasticTechArticles2)
//                .hasSize(pageable.getPageSize())
//                .extracting(ElasticTechArticle::getViewTotalCount)
//                .isSortedAccordingTo(Comparator.reverseOrder())
//                .allMatch(viewCount -> viewCount <= cursor.getViewTotalCount());
//    }
//
//    @Test
//    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그 메인을 댓글수 내림차순으로 조회한다.")
//    void getTechArticlesWithCursorOrderByMOST_COMMENTED() {
//        // given
//        Pageable prevPageable = PageRequest.of(0, 1);
//        Pageable pageable = PageRequest.of(0, 10);
//
//        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.getTechArticles(
//                prevPageable, null,
//                TechArticleSort.MOST_COMMENTED, null, null, null);
//        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//        ElasticTechArticle cursor = elasticTechArticles1.getLast();
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles2 = elasticTechArticleService.getTechArticles(pageable,
//                cursor.getId(), TechArticleSort.MOST_COMMENTED, null, null, null);
//        List<ElasticTechArticle> elasticTechArticles2 = techArticles2.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(elasticTechArticles2)
//                .hasSize(pageable.getPageSize())
//                .extracting(ElasticTechArticle::getCommentTotalCount)
//                .isSortedAccordingTo(Comparator.reverseOrder())
//                .allMatch(commentCount -> commentCount <= cursor.getCommentTotalCount());
//    }
//
//    @Test
//    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그 메인을 인기점수 내림차순으로 조회한다.")
//    void getTechArticlesWithCursorOrderByPOPULAR_SCORE() {
//        // given
//        Pageable prevPageable = PageRequest.of(0, 1);
//        Pageable pageable = PageRequest.of(0, 10);
//
//        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.getTechArticles(
//                prevPageable, null,
//                TechArticleSort.POPULAR, null, null, null);
//        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//        ElasticTechArticle cursor = elasticTechArticles1.getLast();
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles2 = elasticTechArticleService.getTechArticles(pageable,
//                cursor.getId(), TechArticleSort.POPULAR, null, null, null);
//        List<ElasticTechArticle> elasticTechArticles2 = techArticles2.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(elasticTechArticles2)
//                .hasSize(pageable.getPageSize())
//                .extracting(ElasticTechArticle::getPopularScore)
//                .isSortedAccordingTo(Comparator.reverseOrder())
//                .allMatch(popularScore -> popularScore <= cursor.getPopularScore());
//    }
//
//    @Test
//    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그 메인을 정확도순으로 조회하면 최신순으로 조회된다.")
//    void getTechArticlesWithCursorOrderByHIGHEST_SCORE() {
//        // given
//        Pageable prevPageable = PageRequest.of(0, 1);
//        Pageable pageable = PageRequest.of(0, 10);
//
//        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.getTechArticles(
//                prevPageable, null,
//                TechArticleSort.HIGHEST_SCORE, null, null, null);
//        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//        ElasticTechArticle cursor = elasticTechArticles1.getLast();
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles2 = elasticTechArticleService.getTechArticles(pageable,
//                cursor.getId(), TechArticleSort.HIGHEST_SCORE, null, null, null);
//        List<ElasticTechArticle> elasticTechArticles2 = techArticles2.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(elasticTechArticles2)
//                .hasSize(pageable.getPageSize())
//                .extracting(ElasticTechArticle::getRegDate)
//                .isSortedAccordingTo(Comparator.reverseOrder())
//                .allMatch(date -> !date.isAfter(cursor.getRegDate()));
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그를 검색어로 검색한다. (기본정렬은 정확도순)")
//    void getTechArticlesWithKeyword() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//        String keyword = "타이틀";
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null, null,
//                keyword, null, null);
//        List<Float> elasticTechArticlesScores = techArticles.getSearchHits().stream().map(SearchHit::getScore)
//                .toList();
//
//        // then
//        assertThat(techArticles.getSearchHits().size())
//                .isEqualTo(pageable.getPageSize());
//
//        assertThat(elasticTechArticlesScores)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그를 검색어로 검색할 때 정확도 내림차순으로 조회한다.")
//    void getTechArticlesWithKeywordOrderByHIGHEST_SCORE() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//        String keyword = "타이틀";
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null,
//                TechArticleSort.HIGHEST_SCORE, keyword, null, null);
//        List<Float> elasticTechArticlesScores = techArticles.getSearchHits().stream().map(SearchHit::getScore)
//                .toList();
//
//        // then
//        assertThat(techArticles.getSearchHits().size())
//                .isEqualTo(pageable.getPageSize());
//
//        assertThat(elasticTechArticlesScores)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그를 검색어로 검색할 때 최신순 내림차순으로 조회한다.")
//    void getTechArticlesWithKeywordOrderByLATEST() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//        String keyword = "타이틀";
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null,
//                TechArticleSort.LATEST, keyword, null, null);
//        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream().map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(techArticles.getSearchHits().size())
//                .isEqualTo(pageable.getPageSize());
//
//        assertThat(elasticTechArticles)
//                .extracting(ElasticTechArticle::getRegDate)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그를 검색어로 검색할 때 조회순 내림차순으로 조회한다.")
//    void getTechArticlesWithKeywordOrderByMOST_VIEWED() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//        String keyword = "타이틀";
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null,
//                TechArticleSort.MOST_VIEWED, keyword, null, null);
//        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream().map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(techArticles.getSearchHits().size())
//                .isEqualTo(pageable.getPageSize());
//
//        assertThat(elasticTechArticles)
//                .extracting(ElasticTechArticle::getViewTotalCount)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그를 검색어로 검색할 때 댓글순 내림차순으로 조회한다.")
//    void getTechArticlesWithKeywordOrderByMOST_COMMENTED() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//        String keyword = "타이틀";
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null,
//                TechArticleSort.MOST_COMMENTED, keyword, null, null);
//        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream().map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(techArticles.getSearchHits().size())
//                .isEqualTo(pageable.getPageSize());
//
//        assertThat(elasticTechArticles)
//                .extracting(ElasticTechArticle::getCommentTotalCount)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그를 검색어로 검색할 때 인기점수 내림차순으로 조회한다.")
//    void getTechArticlesWithKeywordOrderByPOPULAR_SCORE() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//        String keyword = "타이틀";
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null,
//                TechArticleSort.POPULAR, keyword, null, null);
//        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream().map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(techArticles.getSearchHits().size())
//                .isEqualTo(pageable.getPageSize());
//
//        assertThat(elasticTechArticles)
//                .extracting(ElasticTechArticle::getPopularScore)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그를 검색어로 검색할 때 정확도 내림차순으로 조회한다.")
//    void getTechArticlesWithKeywordWithCursorOrderByHIGHEST_SCORE() {
//        // given
//        Pageable prevPageable = PageRequest.of(0, 1);
//        Pageable pageable = PageRequest.of(0, 10);
//        String keyword = "타이틀";
//
//        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.getTechArticles(
//                prevPageable, null,
//                TechArticleSort.HIGHEST_SCORE, keyword, null, null);
//        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//        List<Float> elasticTechArticleScores1 = techArticles1.getSearchHits().stream().map(SearchHit::getScore)
//                .toList();
//        ElasticTechArticle cursor = elasticTechArticles1.getLast();
//        Float cursorScore = elasticTechArticleScores1.getLast();
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles2 = elasticTechArticleService.getTechArticles(pageable,
//                cursor.getId(), TechArticleSort.HIGHEST_SCORE, keyword, null, cursorScore);
//        List<Float> elasticTechArticleScores2 = techArticles1.getSearchHits().stream().map(SearchHit::getScore)
//                .toList();
//
//        // then
//        assertThat(techArticles2.getSearchHits().size())
//                .isEqualTo(pageable.getPageSize());
//
//        assertThat(elasticTechArticleScores2)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그를 검색어로 검색할 때," +
//            "정확도 내림차순으로 조회하기 위한 점수가 없다면 예외가 발생한다.")
//    void getTechArticlesWithKeywordWithCursorOrderByHIGHEST_SCOREWithoutScoreException() {
//        // given
//        Pageable prevPageable = PageRequest.of(0, 1);
//        Pageable pageable = PageRequest.of(0, 10);
//        String keyword = "타이틀";
//
//        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.getTechArticles(
//                prevPageable, null,
//                TechArticleSort.HIGHEST_SCORE, keyword, null, null);
//        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//        ElasticTechArticle cursor = elasticTechArticles1.getLast();
//
//        // when // then
//        assertThatThrownBy(
//                () -> elasticTechArticleService.getTechArticles(pageable, cursor.getId(), TechArticleSort.HIGHEST_SCORE,
//                        keyword, null, null))
//                .isInstanceOf(ElasticTechArticleException.class)
//                .hasMessage(NOT_FOUND_CURSOR_SCORE_MESSAGE);
//    }
//
//    @Test
//    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그를 검색어로 검색할 때 최신순 내림차순으로 조회한다.")
//    void getTechArticlesWithKeywordWithCursorOrderByLATEST() {
//        // given
//        Pageable prevPageable = PageRequest.of(0, 1);
//        Pageable pageable = PageRequest.of(0, 10);
//        String keyword = "타이틀";
//
//        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.getTechArticles(
//                prevPageable, null,
//                TechArticleSort.LATEST, keyword, null, null);
//        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//        List<Float> elasticTechArticleScores1 = techArticles1.getSearchHits().stream().map(SearchHit::getScore)
//                .toList();
//        ElasticTechArticle cursor = elasticTechArticles1.getLast();
//        Float cursorScore = elasticTechArticleScores1.getLast();
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles2 = elasticTechArticleService.getTechArticles(pageable,
//                cursor.getId(), TechArticleSort.LATEST, keyword, null, cursorScore);
//        List<ElasticTechArticle> elasticTechArticles2 = techArticles2.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(techArticles2.getSearchHits().size())
//                .isEqualTo(pageable.getPageSize());
//
//        assertThat(elasticTechArticles2)
//                .hasSize(pageable.getPageSize())
//                .extracting(ElasticTechArticle::getRegDate)
//                .isSortedAccordingTo(Comparator.reverseOrder())
//                .allMatch(date -> !date.isAfter(cursor.getRegDate()));
//    }
//
//    @Test
//    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그를 검색어로 검색할 때 조회순 내림차순으로 조회한다.")
//    void getTechArticlesWithKeywordWithCursorOrderByMOST_VIEWED() {
//        // given
//        Pageable prevPageable = PageRequest.of(0, 1);
//        Pageable pageable = PageRequest.of(0, 10);
//        String keyword = "타이틀";
//
//        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.getTechArticles(
//                prevPageable, null,
//                TechArticleSort.MOST_VIEWED, keyword, null, null);
//        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//        List<Float> elasticTechArticleScores1 = techArticles1.getSearchHits().stream().map(SearchHit::getScore)
//                .toList();
//        ElasticTechArticle cursor = elasticTechArticles1.getLast();
//        Float cursorScore = elasticTechArticleScores1.getLast();
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles2 = elasticTechArticleService.getTechArticles(pageable,
//                cursor.getId(), TechArticleSort.MOST_VIEWED, keyword, null, cursorScore);
//        List<ElasticTechArticle> elasticTechArticles2 = techArticles2.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(techArticles2.getSearchHits().size())
//                .isEqualTo(pageable.getPageSize());
//
//        assertThat(elasticTechArticles2)
//                .hasSize(pageable.getPageSize())
//                .extracting(ElasticTechArticle::getViewTotalCount)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그를 검색어로 검색할 때 댓글순 내림차순으로 조회한다.")
//    void getTechArticlesWithKeywordWithCursorOrderByMOST_COMMENTED() {
//        // given
//        Pageable prevPageable = PageRequest.of(0, 1);
//        Pageable pageable = PageRequest.of(0, 10);
//        String keyword = "타이틀";
//
//        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.getTechArticles(
//                prevPageable, null,
//                TechArticleSort.MOST_COMMENTED, keyword, null, null);
//        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//        List<Float> elasticTechArticleScores1 = techArticles1.getSearchHits().stream().map(SearchHit::getScore)
//                .toList();
//        ElasticTechArticle cursor = elasticTechArticles1.getLast();
//        Float cursorScore = elasticTechArticleScores1.getLast();
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles2 = elasticTechArticleService.getTechArticles(pageable,
//                cursor.getId(), TechArticleSort.MOST_COMMENTED, keyword, null, cursorScore);
//        List<ElasticTechArticle> elasticTechArticles2 = techArticles2.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(techArticles2.getSearchHits().size())
//                .isEqualTo(pageable.getPageSize());
//
//        assertThat(elasticTechArticles2)
//                .hasSize(pageable.getPageSize())
//                .extracting(ElasticTechArticle::getCommentTotalCount)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그를 검색어로 검색할 때 인기점수 내림차순으로 조회한다.")
//    void getTechArticlesWithKeywordWithCursorOrderByPOPULAR_SCORE() {
//        // given
//        Pageable prevPageable = PageRequest.of(0, 1);
//        Pageable pageable = PageRequest.of(0, 10);
//        String keyword = "타이틀";
//
//        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.getTechArticles(
//                prevPageable, null,
//                TechArticleSort.POPULAR, keyword, null, null);
//        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//        List<Float> elasticTechArticleScores1 = techArticles1.getSearchHits().stream().map(SearchHit::getScore)
//                .toList();
//        ElasticTechArticle cursor = elasticTechArticles1.getLast();
//        Float cursorScore = elasticTechArticleScores1.getLast();
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles2 = elasticTechArticleService.getTechArticles(pageable,
//                cursor.getId(), TechArticleSort.POPULAR, keyword, null, cursorScore);
//        List<ElasticTechArticle> elasticTechArticles2 = techArticles2.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(techArticles2.getSearchHits().size())
//                .isEqualTo(pageable.getPageSize());
//
//        assertThat(elasticTechArticles2)
//                .hasSize(pageable.getPageSize())
//                .extracting(ElasticTechArticle::getPopularScore)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그 메인을 회사로 필터링한 후 최신순으로 조회한다.")
//    void getTechArticlesFilterByCompanyOrderByLATEST() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null,
//                TechArticleSort.LATEST, null, company.getId(), null);
//        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream().map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(elasticTechArticles)
//                .hasSize(pageable.getPageSize())
//                .allSatisfy(article -> {
//                    assertThat(article.getCompanyId()).isEqualTo(company.getId());
//                })
//                .extracting(ElasticTechArticle::getRegDate)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그 메인을 회사로 필터링한 후 조회수 내림차순으로 조회한다.")
//    void getTechArticlesFilterByCompanyOrderByMOST_VIEWED() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null,
//                TechArticleSort.MOST_VIEWED, null, company.getId(), null);
//        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream().map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(elasticTechArticles)
//                .hasSize(pageable.getPageSize())
//                .allSatisfy(article -> {
//                    assertThat(article.getCompanyId()).isEqualTo(company.getId());
//                })
//                .extracting(ElasticTechArticle::getViewTotalCount)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그 메인을 회사로 필터링한 후 댓글수 내림차순으로 조회한다.")
//    void getTechArticlesFilterByCompanyOrderByMOST_COMMENTED() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null,
//                TechArticleSort.MOST_COMMENTED, null, company.getId(), null);
//        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream().map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(elasticTechArticles)
//                .hasSize(pageable.getPageSize())
//                .allSatisfy(article -> {
//                    assertThat(article.getCompanyId()).isEqualTo(company.getId());
//                })
//                .extracting(ElasticTechArticle::getCommentTotalCount)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 기술블로그 메인을 회사로 필터링한 후 인기점수 내림차순으로 조회한다.")
//    void getTechArticlesFilterByCompanyOrderByPOPULAR_SCORE() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.getTechArticles(pageable,
//                null,
//                TechArticleSort.POPULAR, null, company.getId(), null);
//        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream().map(SearchHit::getContent)
//                .toList();
//
//        // then
//        assertThat(elasticTechArticles)
//                .hasSize(pageable.getPageSize())
//                .allSatisfy(article -> {
//                    assertThat(article.getCompanyId()).isEqualTo(company.getId());
//                })
//                .extracting(ElasticTechArticle::getPopularScore)
//                .isSortedAccordingTo(Comparator.reverseOrder());
//    }
//
//    @ParameterizedTest
//    @ValueSource(strings = {"!", "^", "(", ")", "-", "+", "/", "[", "]", "{", "}", ":"})
//    @DisplayName("엘라스틱서치로 키워드 검색을 할 때, 키워드에 특정 특수문자가 있다면 예외가 발생한다.")
//    void getTechArticlesWithSpecialSymbolsException(String keyword) {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when // then
//        assertThatThrownBy(
//                () -> elasticTechArticleService.getTechArticles(pageable, null, null, keyword, null, null))
//                .isInstanceOf(UncategorizedElasticsearchException.class);
//    }
//
//    @ParameterizedTest
//    @ValueSource(strings = {"@", "=", "#", "$", "%", "&", "*", "_", "=", "<", ">", ",", ".", "?", ";", "", "'"})
//    @DisplayName("엘라스틱서치로 키워드 검색을 할 때, 키워드에 특정 특수문자가 아닌 문자들이 있다면 예외가 발생하지 않는다.")
//    void getTechArticlesWithSpecialSymbolsDoesNotThrowException(String keyword) {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when // then
//        assertThatCode(() -> elasticTechArticleService.getTechArticles(pageable, null, null, keyword, null, null))
//                .doesNotThrowAnyException();
//    }
//
//    @Test
//    @DisplayName("엘라스틱서치 키워드 검색시 기본 쿼리 옵션은 AND로 동작한다.")
//    void test() {
//        // given
//        List<ElasticTechArticle> elasticTechArticles = new ArrayList<>();
//        elasticTechArticles.add(ElasticTechArticle.builder().title("자바").build());
//        elasticTechArticles.add(ElasticTechArticle.builder().title("스프링").build());
//        elasticTechArticles.add(ElasticTechArticle.builder().title("자바 스프링").build());
//        elasticTechArticleRepository.saveAll(elasticTechArticles);
//
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when
//        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.getTechArticles(pageable,
//                null, null, "자바", null, null);
//        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//
//        SearchHits<ElasticTechArticle> techArticles2 = elasticTechArticleService.getTechArticles(pageable,
//                null, null, "스프링", null, null);
//        List<ElasticTechArticle> elasticTechArticles2 = techArticles2.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//
//        SearchHits<ElasticTechArticle> techArticles3 = elasticTechArticleService.getTechArticles(pageable,
//                null, null, "자바 스프링", null, null);
//        List<ElasticTechArticle> elasticTechArticles3 = techArticles3.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .toList();
//
//        // then
//        // "자바" 키워드 검색
//        assertThat(elasticTechArticles1)
//                .hasSize(2)
//                .allSatisfy(article ->
//                        assertThat((article.getContents() != null && article.getContents().contains("자바")) ||
//                                (article.getTitle() != null && article.getTitle().contains("자바"))).isTrue());
//        // "스프링" 키워드 검색
//        assertThat(elasticTechArticles2)
//                .hasSize(2)
//                .allSatisfy(article ->
//                        assertThat((article.getContents() != null && article.getContents().contains("스프링")) ||
//                                (article.getTitle() != null && article.getTitle().contains("스프링"))).isTrue());
//        // "자바 스프링" 키워드 검색
//        assertThat(elasticTechArticles3)
//                .hasSize(1)
//                .allSatisfy(article ->
//                        assertThat((article.getContents() != null
//                                && article.getContents().contains("자바") && article.getContents().contains("스프링"))
//                                || (article.getTitle() != null
//                                && article.getTitle().contains("자바") && article.getTitle().contains("스프링"))).isTrue());
//    }
//}