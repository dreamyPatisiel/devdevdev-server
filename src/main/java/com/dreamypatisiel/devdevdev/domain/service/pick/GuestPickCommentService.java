package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.GuestExceptionMessage.INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickRepliedCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentRecommendResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuestPickCommentService implements PickCommentService {


    @Override
    public PickCommentResponse registerPickComment(Long pickId, RegisterPickCommentRequest pickMainCommentRequest,
                                                   Authentication authentication) {
        
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public PickCommentResponse registerPickRepliedComment(Long pickCommentParentId, Long pickCommentOriginParentId,
                                                          Long pickId,
                                                          RegisterPickRepliedCommentRequest pickSubCommentRequest,
                                                          Authentication authentication) {

        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public PickCommentResponse modifyPickComment(Long pickCommentId, Long pickId,
                                                 ModifyPickCommentRequest modifyPickCommentRequest,
                                                 Authentication authentication) {

        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public PickCommentResponse deletePickComment(Long pickCommentId, Long pickId, Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public SliceCustom<PickCommentsResponse> findPickComments(Pageable pageable, Long pickId, Long pickCommentId,
                                                              PickCommentSort pickCommentSort,
                                                              PickOptionType pickOptionType,
                                                              Authentication authentication) {

        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public PickCommentRecommendResponse recommendPickComment(Long pickId, Long pickCommendId,
                                                             Authentication authentication) {

        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }
}
