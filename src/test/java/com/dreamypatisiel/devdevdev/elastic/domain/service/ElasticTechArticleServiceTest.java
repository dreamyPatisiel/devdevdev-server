package com.dreamypatisiel.devdevdev.elastic.domain.service;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.exception.ElasticTechArticleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ElasticTechArticleServiceTest extends ElasticsearchSupportTest {

    @Autowired
    ElasticTechArticleService elasticTechArticleService;
    @Autowired
    TechArticleRepository techArticleRepository;

    @Test
    @DisplayName("커서 방식으로 엘라스틱서치 기술블로그 메인을 조회한다.")
    void findTechArticles() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.findTechArticles(pageable, null, null);

        // then
        assertThat(techArticles)
                .hasSize(pageable.getPageSize());
    }

    @Test
    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그 메인을 조회할 때, " +
                 "존재하지 않는 엘라스틱 기술블로그 ID라면 예외가 발생한다.")
    void findTechArticlesWithCursorException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when // then
        assertThatThrownBy(() -> elasticTechArticleService.findTechArticles(pageable, "dontExistElasticId", null))
                .isInstanceOf(ElasticTechArticleException.class)
                .hasMessage(ElasticTechArticleService.NOT_FOUND_ELASTIC_TECH_ARTICLE_EXCEPTION_MESSAGE);
    }

    @Test
    @DisplayName("커서 방식으로 엘라스틱서치 기술블로그 메인을 최신순으로 조회한다.")
    void findTechArticlesOrderByLATEST() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.findTechArticles(pageable, null, TechArticleSort.LATEST);
        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream()
                .map(SearchHit::getContent)
                        .toList();

        // then
        assertThat(elasticTechArticles)
                .hasSize(pageable.getPageSize())
                .extracting(ElasticTechArticle::getRegDate)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그 메인을 최신순으로 조회한다.")
    void findTechArticlesWithCursorOrderByLATEST() {
        // given
        Pageable pageable1 = PageRequest.of(0, 1);
        Pageable pageable2 = PageRequest.of(0, 10);

        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.findTechArticles(pageable1, null, TechArticleSort.LATEST);
        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
        ElasticTechArticle cursor = elasticTechArticles1.getLast();

        // when
        SearchHits<ElasticTechArticle> techArticles2 = elasticTechArticleService.findTechArticles(pageable2, cursor.getId(), TechArticleSort.LATEST);
        List<ElasticTechArticle> elasticTechArticles2 = techArticles2.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        // then
        assertThat(elasticTechArticles2)
                .hasSize(pageable2.getPageSize())
                .extracting(ElasticTechArticle::getRegDate)
                .isSortedAccordingTo(Comparator.reverseOrder())
                .allMatch(date -> !date.isAfter(cursor.getRegDate()));
    }


    @Test
    @DisplayName("커서 방식으로 엘라스틱서치 기술블로그 메인을 조회수 내림차순으로 조회한다.")
    void findTechArticlesOrderByMOST_VIEWED() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.findTechArticles(pageable, null, TechArticleSort.MOST_VIEWED);
        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        // then
        assertThat(elasticTechArticles)
                .hasSize(pageable.getPageSize())
                .extracting(ElasticTechArticle::getViewTotalCount)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그 메인을 조회수 내림차순으로 조회한다.")
    void findTechArticlesWithCursorOrderByMOST_VIEWED() {
        // given
        Pageable pageable1 = PageRequest.of(0, 1);
        Pageable pageable2 = PageRequest.of(0, 10);

        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.findTechArticles(pageable1, null, TechArticleSort.MOST_VIEWED);
        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
        ElasticTechArticle cursor = elasticTechArticles1.getLast();

        // when
        SearchHits<ElasticTechArticle> techArticles2 = elasticTechArticleService.findTechArticles(pageable2, cursor.getId(), TechArticleSort.MOST_VIEWED);
        List<ElasticTechArticle> elasticTechArticles2 = techArticles2.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        // then
        assertThat(elasticTechArticles2)
                .hasSize(pageable2.getPageSize())
                .extracting(ElasticTechArticle::getViewTotalCount)
                .isSortedAccordingTo(Comparator.reverseOrder())
                .allMatch(viewCount -> viewCount <= cursor.getViewTotalCount());
    }

    @Test
    @DisplayName("커서 방식으로 엘라스틱서치 기술블로그 메인을 댓글수 내림차순으로 조회한다.")
    void findTechArticlesOrderByMOST_COMMENTED() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.findTechArticles(pageable, null, TechArticleSort.MOST_COMMENTED);
        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        // then
        assertThat(elasticTechArticles)
                .hasSize(pageable.getPageSize())
                .extracting(ElasticTechArticle::getCommentTotalCount)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그 메인을 댓글수 내림차순으로 조회한다.")
    void findTechArticlesWithCursorOrderByMOST_COMMENTED() {
        // given
        Pageable pageable1 = PageRequest.of(0, 1);
        Pageable pageable2 = PageRequest.of(0, 10);

        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.findTechArticles(pageable1, null, TechArticleSort.MOST_COMMENTED);
        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
        ElasticTechArticle cursor = elasticTechArticles1.getLast();

        // when
        SearchHits<ElasticTechArticle> techArticles2 = elasticTechArticleService.findTechArticles(pageable2, cursor.getId(), TechArticleSort.MOST_COMMENTED);
        List<ElasticTechArticle> elasticTechArticles2 = techArticles2.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        // then
        assertThat(elasticTechArticles2)
                .hasSize(pageable2.getPageSize())
                .extracting(ElasticTechArticle::getCommentTotalCount)
                .isSortedAccordingTo(Comparator.reverseOrder())
                .allMatch(commentCount -> commentCount <= cursor.getCommentTotalCount());
    }

    @Test
    @DisplayName("커서 방식으로 엘라스틱서치 기술블로그 메인을 인기점수 내림차순으로 조회한다.")
    void findTechArticlesOrderByPOPULAR_SCORE() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.findTechArticles(pageable, null, TechArticleSort.POPULAR);
        List<ElasticTechArticle> elasticTechArticles = techArticles.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        // then
        assertThat(elasticTechArticles)
                .hasSize(pageable.getPageSize())
                .extracting(ElasticTechArticle::getPopularScore)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그 메인을 인기점수 내림차순으로 조회한다.")
    void findTechArticlesWithCursorOrderByPOPULAR_SCORE() {
        // given
        Pageable pageable1 = PageRequest.of(0, 1);
        Pageable pageable2 = PageRequest.of(0, 10);

        SearchHits<ElasticTechArticle> techArticles1 = elasticTechArticleService.findTechArticles(pageable1, null, TechArticleSort.POPULAR);
        List<ElasticTechArticle> elasticTechArticles1 = techArticles1.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
        ElasticTechArticle cursor = elasticTechArticles1.getLast();

        // when
        SearchHits<ElasticTechArticle> techArticles2 = elasticTechArticleService.findTechArticles(pageable2, cursor.getId(), TechArticleSort.POPULAR);
        List<ElasticTechArticle> elasticTechArticles2 = techArticles2.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        // then
        assertThat(elasticTechArticles2)
                .hasSize(pageable2.getPageSize())
                .extracting(ElasticTechArticle::getPopularScore)
                .isSortedAccordingTo(Comparator.reverseOrder())
                .allMatch(popularScore -> popularScore <= cursor.getPopularScore());
    }
}