package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.repository.comment.MyWrittenCommentDto;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentSort;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface TechCommentRepositoryCustom {
    Slice<TechComment> findOriginParentTechCommentsByCursor(Long techArticleId, Long techCommentId,
                                                            TechCommentSort techCommentSort, Pageable pageable);

    List<TechComment> findOriginParentTechBestCommentsByTechArticleIdAndOffset(Long techArticleId, int size);

    SliceCustom<MyWrittenCommentDto> findMyWrittenTechCommentsByCursor(Long memberId, Long techCommentId,
                                                                       Pageable pageable);
}
