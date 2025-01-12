package com.dreamypatisiel.devdevdev.domain.repository.pick.custom;

import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.repository.comment.MyWrittenCommentDto;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import java.util.EnumSet;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PickCommentRepositoryCustom {
    Slice<PickComment> findOriginParentPickCommentsByCursor(Pageable pageable, Long pickId, Long pickCommentId,
                                                            PickCommentSort pickCommentSort,
                                                            EnumSet<PickOptionType> pickOptionTypes);

    List<PickComment> findOriginParentPickBestCommentsByPickIdAndOffset(Long pickId, int offset);

    List<PickComment> findOriginParentPickCommentsByPickIdAndPickOptionTypeIn(Long pickId,
                                                                              EnumSet<PickOptionType> pickOptionTypes);

    SliceCustom<MyWrittenCommentDto> findMyWrittenPickCommentsByCursor(Long memberId, Long pickCommentId,
                                                                       Pageable pageable);
}
