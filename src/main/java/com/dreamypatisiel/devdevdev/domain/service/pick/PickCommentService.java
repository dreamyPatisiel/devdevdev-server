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
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface PickCommentService {
    PickCommentResponse registerPickComment(Long pickId,
                                            RegisterPickCommentRequest pickMainCommentRequest,
                                            Authentication authentication);

    public PickCommentResponse registerPickRepliedComment(Long pickCommentParentId,
                                                          Long pickCommentOriginParentId,
                                                          Long pickId,
                                                          RegisterPickRepliedCommentRequest pickSubCommentRequest,
                                                          Authentication authentication);

    public PickCommentResponse modifyPickComment(Long pickCommentId, Long pickId,
                                                 ModifyPickCommentRequest modifyPickCommentRequest,
                                                 Authentication authentication);

    public PickCommentResponse deletePickComment(Long pickCommentId, Long pickId, Authentication authentication);

    public SliceCustom<PickCommentsResponse> findPickComments(Pageable pageable, Long pickId,
                                                              Long pickCommentId, PickCommentSort pickCommentSort,
                                                              PickOptionType pickOptionType,
                                                              Authentication authentication);

    public PickCommentRecommendResponse recommendPickComment(Long pickId, Long pickCommendId,
                                                             Authentication authentication);
}
