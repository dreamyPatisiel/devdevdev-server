package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_ANONYMOUS_MEMBER_ID_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_CAN_NOT_VOTE_SAME_PICK_OPTION_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.member.AnonymousMemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.service.pick.dto.VotePickOptionDto;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.VotePickOptionException;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.openai.embeddings.EmbeddingsService;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickDetailOptionResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickModifyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickRegisterResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickUploadImageResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.SimilarPickResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.VotePickOptionResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.VotePickResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
@Transactional(readOnly = true)
public class GuestPickService extends PickCommonService implements PickService {

    public static final String INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE = "비회원은 현재 해당 기능을 이용할 수 없습니다.";
    public static final int SIMILARITY_PICK_MAX_COUNT = 3;

    private final PickPopularScorePolicy pickPopularScorePolicy;
    private final PickVoteRepository pickVoteRepository;
    private final AnonymousMemberRepository anonymousMemberRepository;

    public GuestPickService(PickRepository pickRepository, EmbeddingsService embeddingsService,
                            PickCommentRepository pickCommentRepository,
                            PickPopularScorePolicy pickPopularScorePolicy,
                            PickVoteRepository pickVoteRepository,
                            AnonymousMemberRepository anonymousMemberRepository) {
        super(embeddingsService, pickRepository, pickCommentRepository);
        this.pickPopularScorePolicy = pickPopularScorePolicy;
        this.pickVoteRepository = pickVoteRepository;
        this.anonymousMemberRepository = anonymousMemberRepository;
    }

    @Transactional
    @Override
    public Slice<PickMainResponse> findPicksMain(Pageable pageable, Long pickId, PickSort pickSort,
                                                 String anonymousMemberId, Authentication authentication) {
        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // anonymousMemberId 검증
        AnonymousMember anonymousMember = findOrCreateAnonymousMember(anonymousMemberId);

        // 픽픽픽 조회
        Slice<Pick> picks = pickRepository.findPicksByCursor(pageable, pickId, pickSort);

        // 데이터 가공
        List<PickMainResponse> pickMainResponse = picks.stream()
                .map(pick -> PickMainResponse.of(pick, anonymousMember))
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
    public PickDetailResponse findPickDetail(Long pickId, String anonymousMemberId, Authentication authentication) {

        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        // 익명 회원 조회 또는 생성
        AnonymousMember anonymousMember = findOrCreateAnonymousMember(anonymousMemberId);

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
                        pickOption -> PickDetailOptionResponse.of(pickOption, findPick, anonymousMember)));

        // 픽픽픽 상세
        return PickDetailResponse.of(findPick, findPick.getMember(), anonymousMember, pickDetailOptions);
    }

    private AnonymousMember findOrCreateAnonymousMember(String anonymousMemberId) {
        // 익명 사용자 검증
        validateAnonymousMemberId(anonymousMemberId);

        // 익명회원 조회 또는 생성
        return anonymousMemberRepository.findByAnonymousMemberId(anonymousMemberId)
                .orElseGet(() -> anonymousMemberRepository.save(AnonymousMember.create(anonymousMemberId)));
    }

    /**
     * 익명 회원이 픽픽픽을 투표한다.
     */
    @Transactional
    @Override
    public VotePickResponse votePickOption(VotePickOptionDto votePickOptionDto,
                                           Authentication authentication) {

        // 익명 사용자 호출인지 확인
        AuthenticationMemberUtils.validateAnonymousMethodCall(authentication);

        Long pickId = votePickOptionDto.getPickId();
        Long pickOptionId = votePickOptionDto.getPickOptionId();
        String anonymousMemberId = votePickOptionDto.getAnonymousMemberId();

        // 익명 회원을 조회하거나 생성
        AnonymousMember anonymousMember = findOrCreateAnonymousMember(anonymousMemberId);

        Optional<PickVote> pickVoteOptional = pickVoteRepository.findWithPickAndPickOptionByPickIdAndAnonymousMember(
                pickId,
                anonymousMember);

        return pickVoteOptional
                // 픽픽픽 투표 이력이 있는 경우
                .map(pickVote -> getVotePickResponseAndHandlePickVoteAndPickOptionExistingPickVoteOnPickOption(
                        pickVote, pickOptionId, pickId, anonymousMember)
                )
                // 픽픽픽 투표 이력이 없는 경우
                .orElseGet(() -> getVoteResponseAndHandlePickVoteAndPickOptionNotExistingPickVoteOnPickOption(
                        pickId, pickOptionId, anonymousMember)
                );
    }

    // 픽픽픽 투표 이력이 있는 경우
    private VotePickResponse getVotePickResponseAndHandlePickVoteAndPickOptionExistingPickVoteOnPickOption(
            PickVote pickVote, Long pickOptionId, Long pickId, AnonymousMember anonymousMember) {

        PickOption pickOption = pickVote.getPickOption();

        // 같은 픽픽픽 옵션에 투표 했을 경우(예외 발생)
        if (pickOption.isEqualsId(pickOptionId)) {
            throw new VotePickOptionException(INVALID_CAN_NOT_VOTE_SAME_PICK_OPTION_MESSAGE);
        }

        // 다른 픽픽픽 옵션에 투표 했을 경우
        Pick findPick = pickRepository.findPickWithPickOptionByPickId(pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_MESSAGE));

        // 데이터 가공 및 로직 수행
        List<VotePickOptionResponse> votePickOptionsResponse = findPick.getPickOptions().stream()
                .map(findPickOption -> {
                    // 투표하고자 하는 픽 옵션이면 투표를 생성
                    if (findPickOption.isEqualsId(pickOptionId)) {
                        return getVotePickOptionResponseAndCreatePickVote(findPickOption, findPick, anonymousMember);
                    }
                    // 기존 투표의 픽 옵션의 아이디와 일치하는 경우 투표 삭제
                    else if (findPickOption.isEqualsId(pickVote.getPickOption().getId())) {
                        return getVotePickOptionResponseAndDeletePickVote(pickOption, findPick, pickVote);
                    }
                    // 그외 투표 패스(현재 픽 옵션이 2개로 고정되어 있는데 2+N 개로 늘어 날 수 있음. 그 경우에 대해서는 아래와 같은 응답을 준다.)
                    return getDefaultVotePickOptionResponse(findPickOption, findPick);
                })
                .collect(Collectors.toList());

        // 인기 점수 계산
        findPick.changePopularScore(pickPopularScorePolicy);

        return VotePickResponse.of(findPick.getId(), votePickOptionsResponse);
    }

    // 투표 패스 기본 응답
    private VotePickOptionResponse getDefaultVotePickOptionResponse(PickOption pickOption, Pick pick) {
        BigDecimal percent = PickOption.calculatePercentBy(pick, pickOption);
        return VotePickOptionResponse.of(pickOption, null, percent, false);
    }

    // 픽픽픽 투표 이력이 없는 경우
    private VotePickResponse getVoteResponseAndHandlePickVoteAndPickOptionNotExistingPickVoteOnPickOption(Long pickId,
                                                                                                          Long pickOptionId,
                                                                                                          AnonymousMember anonymousMember) {
        // 픽픽픽 조회
        Pick findPick = pickRepository.findPickWithPickOptionByPickId(pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_MESSAGE));

        // 픽픽픽 전체 득표수 증가
        findPick.incrementVoteTotalCount();

        // 데이터 가공 및 로직 수행
        List<VotePickOptionResponse> votePickOptionResponses = findPick.getPickOptions().stream()
                .map(findPickOption -> {
                    // 투표하고자 하는 픽 옵션이면 투표를 생성
                    if (findPickOption.isEqualsId(pickOptionId)) {
                        return getVotePickOptionResponseAndCreatePickVote(findPickOption, findPick, anonymousMember);
                    }
                    // 득표율 계산
                    return getDefaultVotePickOptionResponse(findPickOption, findPick);
                })
                .toList();

        // 인기 점수 계산
        findPick.changePopularScore(pickPopularScorePolicy);

        return VotePickResponse.of(findPick.getId(), votePickOptionResponses);
    }

    // 투표 생성 로직
    private VotePickOptionResponse getVotePickOptionResponseAndCreatePickVote(PickOption pickOption, Pick pick,
                                                                              AnonymousMember anonymousMember) {
        // 투표 생성
        PickVote newPickVote = PickVote.createByAnonymous(anonymousMember, pickOption.getPick(), pickOption);
        pickVoteRepository.save(newPickVote);

        // 투표수 증가
        pickOption.plusOneVoteTotalCount();

        // 득표율 계산
        BigDecimal percent = PickOption.calculatePercentBy(pick, pickOption);

        return VotePickOptionResponse.of(pickOption, newPickVote.getId(), percent, true);
    }

    // 투표 삭제 로직
    private VotePickOptionResponse getVotePickOptionResponseAndDeletePickVote(PickOption findPickOption, Pick findPick,
                                                                              PickVote pickVote) {
        // 기존 픽픽픽 옵션 투표수 감소
        findPickOption.minusVoteTotalCount();

        // 투표 삭제
        pickVoteRepository.delete(pickVote);

        // 득표율 계산
        BigDecimal percent = PickOption.calculatePercentBy(findPick, findPickOption);

        return VotePickOptionResponse.of(findPickOption, null, percent, false);
    }

    @Override
    public void deletePick(Long pickId, Authentication authentication) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Override
    public List<SimilarPickResponse> findTop3SimilarPicks(Long pickId) {
        return super.findTop3SimilarPicks(pickId);
    }

    private void validateAnonymousMemberId(String anonymousMemberId) {
        if (!StringUtils.hasText(anonymousMemberId)) {
            throw new IllegalArgumentException(INVALID_ANONYMOUS_MEMBER_ID_MESSAGE);
        }
    }

    @Override
    public void deleteImage(Long pickOptionImageId) {
        throw new AccessDeniedException(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }
}
