package com.dreamypatisiel.devdevdev.domain.service.techArticle.techComment;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentSort;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.ModifyTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.RegisterTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentRecommendResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentsResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

public interface TechCommentService {

    TechCommentResponse registerMainTechComment(Long techArticleId,
                                                RegisterTechCommentRequest registerTechCommentRequest,
                                                Authentication authentication);

    TechCommentResponse registerRepliedTechComment(Long techArticleId,
                                                   Long originParentTechCommentId,
                                                   Long parentTechCommentId,
                                                   RegisterTechCommentRequest registerRepliedTechCommentRequest,
                                                   Authentication authentication);

    TechCommentResponse modifyTechComment(Long techArticleId, Long techCommentId,
                                          ModifyTechCommentRequest modifyTechCommentRequest,
                                          Authentication authentication);

    TechCommentResponse deleteTechComment(Long techArticleId, Long techCommentId, Authentication authentication);

    SliceCustom<TechCommentsResponse> getTechComments(Long techArticleId, Long techCommentId,
                                                      TechCommentSort techCommentSort, Pageable pageable,
                                                      Authentication authentication);

    TechCommentRecommendResponse recommendTechComment(Long techArticleId, Long techCommentId,
                                                      Authentication authentication);
}
