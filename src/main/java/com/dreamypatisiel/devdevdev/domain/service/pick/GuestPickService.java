package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_ANONYMOUS_MEMBER_ID_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_CAN_NOT_VOTE_SAME_PICK_OPTION_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.AnonymousMemberRepository;
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
import com.dreamypatisiel.devdevdev.domain.service.response.VotePickOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.VotePickResponse;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.VotePickOptionException;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.controller.request.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.VotePickOptionRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
    private final AnonymousMemberRepository anonymousMemberRepository;

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

    @Transactional
    @Override
    public PickDetailResponse findPickDetail(Long pickId, Authentication authentication) {

        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 픽픽픽 상세 조회(pickOption 페치조인)
        Pick findPick = pickRepository.findPickWithPickOptionByPickId(pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_MESSAGE));

        // 픽픽픽 게시글의 승인 상태가 아니면
        if (!findPick.isTrueContentStatus(ContentStatus.APPROVAL)) {
            throw new IllegalArgumentException(INVALID_NOT_APPROVAL_STATUS_PICK_MESSAGE);
        }

        findPick.plusOneViewTotalCount(); // 조회수 증가
        findPick.changePopularScore(pickPopularScorePolicy); // 인기점수 계산

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

        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        Long pickId = votePickOptionRequest.getPickId();
        Long pickOptionId = votePickOptionRequest.getPickOptionId();
        String anonymousMemberId = votePickOptionRequest.getAnonymousMemberId();

        if (!StringUtils.hasText(anonymousMemberId)) {
            throw new IllegalArgumentException(INVALID_ANONYMOUS_MEMBER_ID_MESSAGE);
        }

        // 익명회원 조회 또는 생성
        AnonymousMember anonymousMember = anonymousMemberRepository.findByAnonymousMemberId(anonymousMemberId)
                .orElseGet(() -> anonymousMemberRepository.save(AnonymousMember.create(anonymousMemberId)));

        // 픽픽픽 투표 조회
        Optional<PickVote> findOptionalPickVote = pickVoteRepository.findByPickIdAndAnonymousMember(pickId,
                anonymousMember);

        // 픽옵션에 투표를 한 이력이 있는 경우
        if (findOptionalPickVote.isPresent()) {
            PickVote pickVote = findOptionalPickVote.get();
            PickOption findPickOptionByPickVote = pickVote.getPickOption();

            // 투표하려는 픽옵션과 일치하는 경우(이미 투표한 픽옵션에 또 투표할 경우)
            if (findPickOptionByPickVote.isEqualsId(pickOptionId)) {
                throw new VotePickOptionException(INVALID_CAN_NOT_VOTE_SAME_PICK_OPTION_MESSAGE);
            }

            // 투표하려는 픽옵션과 일치하지 않는 경우(투표하지 않은 픽옵션에 투표할 경우)
            pickVoteRepository.delete(pickVote); // 기존 투표 삭제
        }

        // 픽 옵션에 투표한 이력이 없는 경우(투표 생성)
        // 픽픽픽 조회
        Pick findPick = pickRepository.findPickWithPickOptionByPickId(pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_MESSAGE));

        // 인기점수 계산
        findPick.changePopularScore(pickPopularScorePolicy);

        // 픽 옵션 투표 데이터 가공
        List<VotePickOptionResponse> votePickOptionsResponse = findPick.getPickOptions().stream()
                .map(pickOption -> getVotePickOptionResponse(pickOption, findPick, anonymousMember, pickOptionId))
                .toList();

        return VotePickResponse.of(findPick.getId(), votePickOptionsResponse);
    }

    private VotePickOptionResponse getVotePickOptionResponse(PickOption pickOption, Pick findPick,
                                                             AnonymousMember anonymousMember,
                                                             Long pickOptionId) {

        // 해당 픽 옵션에 투표한 경우
        if (pickOption.isEqualsId(pickOptionId)) {
            return getPickedTrueVotePickOptionResponse(pickOption, findPick, anonymousMember);
        }

        // 해당 픽 옵션에 투표하지 않은 경우
        return getPickedFalseVotePickOptionResponse(pickOption, findPick);
    }

    private VotePickOptionResponse getPickedFalseVotePickOptionResponse(PickOption pickOption, Pick findPick) {
        pickOption.minusVoteTotalCount(); // 득표 수 감소
        int percent = PickOption.calculatePercentBy(findPick, pickOption).intValueExact(); // 득표율 계산

        return VotePickOptionResponse.of(pickOption, null, percent, false);
    }

    private VotePickOptionResponse getPickedTrueVotePickOptionResponse(PickOption pickOption, Pick findPick,
                                                                       AnonymousMember anonymousMember) {
        pickOption.plusOneVoteTotalCount(); // 득표 수 증가
        findPick.plusOneVoteTotalCount(); // 득표 수 증가
        int percent = PickOption.calculatePercentBy(findPick, pickOption).intValueExact(); // 득표율 계산

        // 투표 생성
        PickVote pickVote = PickVote.createByAnonymous(anonymousMember, findPick, pickOption);
        pickVoteRepository.save(pickVote);

        return VotePickOptionResponse.of(pickOption, pickVote.getId(), percent, true);
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
