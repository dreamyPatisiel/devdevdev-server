package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickRepliedCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentRecommendResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentsResponse;
import java.util.EnumSet;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface PickCommentService {
    PickCommentResponse registerPickComment(Long pickId,
                                            RegisterPickCommentRequest pickMainCommentRequest,
                                            Authentication authentication);

    PickCommentResponse registerPickRepliedComment(Long pickParentCommentId,
                                                   Long pickCommentOriginParentId,
                                                   Long pickId,
                                                   RegisterPickRepliedCommentRequest pickSubCommentRequest,
                                                   Authentication authentication);

    PickCommentResponse modifyPickComment(Long pickCommentId, Long pickId,
                                          ModifyPickCommentRequest modifyPickCommentRequest,
                                          Authentication authentication);

    PickCommentResponse deletePickComment(Long pickCommentId, Long pickId, Authentication authentication);

    SliceCustom<PickCommentsResponse> findPickComments(Pageable pageable, Long pickId,
                                                       Long pickCommentId, PickCommentSort pickCommentSort,
                                                       EnumSet<PickOptionType> pickOptionTypes,
                                                       Authentication authentication);

    PickCommentRecommendResponse recommendPickComment(Long pickId, Long pickCommendId,
                                                      Authentication authentication);

    List<PickCommentsResponse> findPickBestComments(int size, Long pickId, Authentication authentication);
}
