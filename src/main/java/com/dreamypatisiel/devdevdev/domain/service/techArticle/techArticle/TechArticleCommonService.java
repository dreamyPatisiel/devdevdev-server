package com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle;

import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_TECH_ARTICLE_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TechArticleCommonService {

    private final TechArticleRepository techArticleRepository;

    public TechArticle findTechArticle(Long techArticleId) {
        return techArticleRepository.findById(techArticleId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_TECH_ARTICLE_MESSAGE));
    }

    public static void validateIsDeletedTechComment(TechComment techComment, String message,
                                                    @Nullable String messageArgs) {
        if (techComment.isDeleted()) {
            throw new IllegalArgumentException(String.format(message, messageArgs));
        }
    }
}
