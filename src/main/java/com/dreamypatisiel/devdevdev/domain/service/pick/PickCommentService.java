package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.dreamypatisiel.devdevdev.domain.service.pick.dto.PickCommentDto;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentRecommendResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentsResponse;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface PickCommentService {
    String MODIFY = "수정";
    String REGISTER = "작성";
    String DELETE = "삭제";
    String RECOMMEND = "추천";

    PickCommentResponse registerPickComment(Long pickId, PickCommentDto pickRegisterCommentDto, Authentication authentication);

    PickCommentResponse registerPickRepliedComment(Long pickParentCommentId, Long pickCommentOriginParentId,
                                                   Long pickId, PickCommentDto pickRegisterRepliedCommentDto,
                                                   Authentication authentication);

    PickCommentResponse modifyPickComment(Long pickCommentId, Long pickId, PickCommentDto pickModifyCommentDto,
                                          Authentication authentication);

    PickCommentResponse deletePickComment(Long pickCommentId, Long pickId, @Nullable String anonymousMemberId,
                                          Authentication authentication);

    SliceCustom<PickCommentsResponse> findPickComments(Pageable pageable, Long pickId, Long pickCommentId,
                                                       PickCommentSort pickCommentSort, EnumSet<PickOptionType> pickOptionTypes,
                                                       String anonymousMemberId, Authentication authentication);

    PickCommentRecommendResponse recommendPickComment(Long pickId, Long pickCommendId, Authentication authentication);

    List<PickCommentsResponse> findPickBestComments(int size, Long pickId, String anonymousMemberId,
                                                    Authentication authentication);
}
