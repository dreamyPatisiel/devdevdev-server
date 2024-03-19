package com.dreamypatisiel.devdevdev.elastic.domain.service;

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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ElasticsearchServiceTest extends ElasticsearchSupportTest {

    @Autowired
    ElasticTechArticleService elasticTechArticleService;

    @Test
    @DisplayName("커서 방식으로 엘라스틱서치 기술블로그 메인을 조회한다.")
    void findTechArticles() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.findTechArticles(pageable, null, null);

        // then
        assertThat(techArticles)
                .hasSizeLessThanOrEqualTo(pageable.getPageSize());
    }

    @Test
    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그 메인을 조회한다.")
    void findTechArticlesWithCursor() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        // when
        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.findTechArticles(pageable, null, null);

        // then
        List<ElasticTechArticle> articlesList = techArticles.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        // then
        assertThat(articlesList)
                .hasSizeLessThanOrEqualTo(pageable.getPageSize())
                .extracting(ElasticTechArticle::getRegDate)
                .isSortedAccordingTo(Comparator.reverseOrder());
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

        // then
        List<ElasticTechArticle> articlesList = techArticles.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        // then
        assertThat(articlesList)
                .hasSizeLessThanOrEqualTo(pageable.getPageSize())
                .extracting(ElasticTechArticle::getRegDate)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    @DisplayName("커서 방식으로 엘라스틱서치 기술블로그 메인을 조회수 내림차순으로 조회한다.")
    void findTechArticlesOrderByMOST_VIEWED() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.findTechArticles(pageable, null, TechArticleSort.MOST_VIEWED);

        // then
        List<ElasticTechArticle> articlesList = techArticles.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        // then
        assertThat(articlesList)
                .hasSizeLessThanOrEqualTo(pageable.getPageSize())
                .extracting(ElasticTechArticle::getViewTotalCount)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    @DisplayName("커서 방식으로 엘라스틱서치 기술블로그 메인을 댓글수 내림차순으로 조회한다.")
    void findTechArticlesOrderByCOMMENT_COUNT() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.findTechArticles(pageable, null, TechArticleSort.MOST_COMMENTED);

        // then
        List<ElasticTechArticle> articlesList = techArticles.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        // then
        assertThat(articlesList)
                .hasSizeLessThanOrEqualTo(pageable.getPageSize())
                .extracting(ElasticTechArticle::getCommentTotalCount)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    @DisplayName("커서 방식으로 엘라스틱서치 기술블로그 메인을 인기점수 내림차순으로 조회한다.")
    void findTechArticlesOrderByPOPULAR_SCORE() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        SearchHits<ElasticTechArticle> techArticles = elasticTechArticleService.findTechArticles(pageable, null, TechArticleSort.POPULAR);

        // then
        List<ElasticTechArticle> articlesList = techArticles.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        // then
        assertThat(articlesList)
                .hasSizeLessThanOrEqualTo(pageable.getPageSize())
                .extracting(ElasticTechArticle::getPopularScore)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }
}