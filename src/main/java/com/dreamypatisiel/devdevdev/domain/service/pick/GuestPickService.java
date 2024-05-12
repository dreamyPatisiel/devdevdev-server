package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailOptionImage;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickMainOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickMainResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickModifyResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickRegisterResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickUploadImageResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.VotePickResponse;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.controller.request.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.VotePickOptionRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestPickService implements PickService {

    public static final String INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE = "비회원은 현재 해당 기능을 이용할 수 없습니다.";

    private final PickRepository pickRepository;
    private final PickOptionRepository pickOptionRepository;
    private final PickPopularScorePolicy pickPopularScorePolicy;
    private final PickVoteRepository pickVoteRepository;

    @Override
    public Slice<PickMainResponse> findPicksMain(Pageable pageable, Long pickId, PickSort pickSort,
                                                 Authentication authentication) {
        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 픽픽픽 조회
        Slice<Pick> picks = pickRepository.findPicksByCursor(pageable, pickId, pickSort);

        // 데이터 가공
        List<PickMainResponse> pickMainResponse = picks.stream()
                .map(this::mapToPickResponse)
                .toList();

        return new SliceImpl<>(pickMainResponse, pageable, picks.hasNext());
    }

    @Override
    public PickUploadImageResponse uploadImages(String name, List<MultipartFile> images) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public PickRegisterResponse registerPick(RegisterPickRequest registerPickRequest, Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public PickModifyResponse modifyPick(Long pickId, ModifyPickRequest modifyPickRequest,
                                         Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public PickDetailResponse findPickDetail(Long pickId, Authentication authentication) {

        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 픽픽픽 상세 조회(pickOption 페치조인)
        Pick findPick = pickRepository.findPickWithPickOptionByPickId(pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_MESSAGE));

        // 픽픽픽 옵션 가공
        Map<PickOptionType, PickDetailOptionResponse> pickDetailOptions = findPick.getPickOptions().stream()
                .collect(Collectors.toMap(PickOption::getPickOptionType,
                        pickOption -> mapToPickDetailOptionsResponse(pickOption, findPick)));

        // 픽픽픽 상세
        return PickDetailResponse.of(findPick, findPick.getMember(), pickDetailOptions);

    }

    /**
     * 익명 회원이 픽픽픽을 투표한다.
     */
    @Transactional
    @Override
    public VotePickResponse votePickOption(VotePickOptionRequest votePickOptionRequest,
                                           Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    private PickDetailOptionResponse mapToPickDetailOptionsResponse(PickOption pickOption, Pick findPick) {
        return PickDetailOptionResponse.builder()
                .id(pickOption.getId())
                .title(pickOption.getTitle().getTitle())
                .isPicked(false)
                .percent(PickOption.calculatePercentBy(findPick, pickOption))
                .voteTotalCount(pickOption.getVoteTotalCount().getCount())
                .content(pickOption.getContents().getPickOptionContents())
                .pickDetailOptionImages(mapToPickDetailOptionImagesResponse(pickOption))
                .build();
    }

    private List<PickDetailOptionImage> mapToPickDetailOptionImagesResponse(PickOption pickOption) {
        return pickOption.getPickOptionImages().stream()
                .map(this::mapToPickOptionImageResponse)
                .toList();
    }

    private PickDetailOptionImage mapToPickOptionImageResponse(PickOptionImage pickOptionImage) {
        return PickDetailOptionImage.builder()
                .id(pickOptionImage.getId())
                .imageUrl(pickOptionImage.getImageUrl())
                .build();
    }

    @Override
    public void deleteImage(Long pickOptionImageId) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    private PickMainResponse mapToPickResponse(Pick pick) {
        return PickMainResponse.builder()
                .id(pick.getId())
                .title(pick.getTitle())
                .voteTotalCount(pick.getVoteTotalCount())
                .commentTotalCount(pick.getCommentTotalCount())
                .viewTotalCount(pick.getViewTotalCount())
                .popularScore(pick.getPopularScore())
                .pickOptions(mapToPickOptionsResponse(pick))
                .isVoted(false)
                .build();
    }

    private List<PickMainOptionResponse> mapToPickOptionsResponse(Pick pick) {
        return pick.getPickOptions().stream()
                .map(pickOption -> mapToPickOptionResponse(pick, pickOption))
                .toList();
    }

    private PickMainOptionResponse mapToPickOptionResponse(Pick pick, PickOption pickOption) {
        return PickMainOptionResponse.builder()
                .id(pickOption.getId())
                .title(pickOption.getTitle())
                .percent(PickOption.calculatePercentBy(pick, pickOption))
                .isPicked(false)
                .build();
    }
}
