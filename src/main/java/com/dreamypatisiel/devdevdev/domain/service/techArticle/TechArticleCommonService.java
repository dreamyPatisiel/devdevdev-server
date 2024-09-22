package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_ELASTIC_ID_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_TECH_ARTICLE_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.elastic.data.response.ElasticResponse;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.TechArticleException;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TechArticleCommonService {

    private final TechArticleRepository techArticleRepository;
    private final ElasticTechArticleRepository elasticTechArticleRepository;

    protected ElasticTechArticle findElasticTechArticle(TechArticle techArticle) {
        String elasticId = techArticle.getElasticId();

        if (!StringUtils.hasText(elasticId)) {
            throw new TechArticleException(NOT_FOUND_ELASTIC_ID_MESSAGE);
        }

        return elasticTechArticleRepository.findById(elasticId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE));
    }

    protected TechArticle findTechArticle(Long techArticleId) {
        return techArticleRepository.findById(techArticleId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_TECH_ARTICLE_MESSAGE));
    }

    protected List<TechArticle> findTechArticlesByElasticTechArticles(
            List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse) {
        List<String> elasticIds = getElasticIdsFromElasticTechArticles(elasticTechArticlesResponse);
        // 추출한 elasticId가 없다면 빈 리스트 응답
        if (elasticIds.isEmpty()) {
            return Collections.emptyList();
        }

        return techArticleRepository.findAllByElasticIdIn(elasticIds);
    }

    public List<ElasticTechArticle> findElasticTechArticlesByTechArticles(List<TechArticle> techArticles) {
        List<String> elasticIds = getElasticIdsFromTechArticles(techArticles);
        // 추출한 elasticId가 없다면 빈 리스트 응답
        if (elasticIds.isEmpty()) {
            return Collections.emptyList();
        }

        Iterable<ElasticTechArticle> elasticTechArticles = elasticTechArticleRepository.findAllById(elasticIds);

        return StreamSupport.stream(elasticTechArticles.spliterator(), false)
                .toList();
    }

    private List<String> getElasticIdsFromTechArticles(List<TechArticle> techArticles) {
        return techArticles.stream()
                .map(TechArticle::getElasticId)
                .toList();
    }

    private List<String> getElasticIdsFromElasticTechArticles(
            List<ElasticResponse<ElasticTechArticle>> elasticTechArticlesResponse) {
        return elasticTechArticlesResponse.stream()
                .map(elasticResponse -> elasticResponse.content().getId())
                .toList();
    }

    public static void validateIsDeletedTechComment(TechComment techComment, String message,
                                                    @Nullable String messageArgs) {
        if (techComment.isDeleted()) {
            throw new IllegalArgumentException(String.format(message, messageArgs));
        }
    }
}
