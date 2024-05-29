package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_CAN_NOT_VOTE_SAME_PICK_OPTION_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_MODIFY_MEMBER_PICK_ONLY_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_CAN_MODIFY_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_OPTION_IMAGE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_PICK_OPTION_IMAGE_NAME_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_PICK_OPTION_IMAGE_SIZE_MESSAGE;

import com.dreamypatisiel.devdevdev.aws.s3.AwsS3Uploader;
import com.dreamypatisiel.devdevdev.aws.s3.S3ImageObject;
import com.dreamypatisiel.devdevdev.aws.s3.properties.AwsS3Properties;
import com.dreamypatisiel.devdevdev.aws.s3.properties.S3;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.service.pick.dto.VotePickOptionDto;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickMainResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickModifyResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickRegisterResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickUploadImageResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.VotePickOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.VotePickResponse;
import com.dreamypatisiel.devdevdev.exception.ImageFileException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.PickOptionImageNameException;
import com.dreamypatisiel.devdevdev.exception.VotePickOptionException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.web.controller.request.ModifyPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterPickRequest;
import java.math.BigDecimal;
import java.util.Collections;
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
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPickService implements PickService {

    public static final String FIRST_PICK_OPTION_IMAGE = "firstPickOptionImage";
    public static final String SECOND_PICK_OPTION_IMAGE = "secondPickOptionImage";
    public static final int MAX_IMAGE_SIZE = 3;
    public static final int START_INCLUSIVE = 0;

    private final AwsS3Properties awsS3Properties;
    private final AwsS3Uploader awsS3Uploader;
    private final MemberProvider memberProvider;
    private final PickRepository pickRepository;
    private final PickOptionRepository pickOptionRepository;
    private final PickOptionImageRepository pickOptionImageRepository;
    private final PickVoteRepository pickVoteRepository;
    private final PickPopularScorePolicy pickPopularScorePolicy;

    /**
     * 픽픽픽 메인 조회
     */
    @Override
    public Slice<PickMainResponse> findPicksMain(Pageable pageable, Long pickId, PickSort pickSort,
                                                 String anonymousMemberId, Authentication authentication) {
        // 픽픽픽 조회
        Slice<Pick> picks = pickRepository.findPicksByCursor(pageable, pickId, pickSort);

        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 데이터 가공
        List<PickMainResponse> pickMainResponse = picks.stream()
                .map(pick -> PickMainResponse.of(pick, member))
                .toList();

        return new SliceImpl<>(pickMainResponse, pageable, picks.hasNext());
    }

    /**
     * @Note: 이미지 업로드와 DB 저장을 하나의 작업(Transcation)으로 묶어서, 데이터 정합성을 유지한다.<br/> 이때 pick_option_id는 null 인 상태
     * 입니다.<br/><br/>
     * <p>
     * 이미지 업로드 실패시 IOException이 발생할 수 있는데, 이때 catch로 처리하여 데이터 정합성 유지합니다.<br/> 즉, IOException이 발생해도 rollback하지 않는다는 의미
     * 입니다. <br/><br/>
     * <p>
     * 단, Transcation이 길게 유지되면 추후 DB Connection을 오랫동안 유지하기 때문에 많은 트래픽이 발생할 때 DB Connection이 부족해지는 현상이 발생할 수
     * 있습니다.<br/><br/>
     * <p>
     * (Transcation은 기본적으로 RuntimeException에 대해서만 Rollback 합니다. AmazonClient의 putObject(...)는 RuntimeException을
     * 발생시킵니다.)<br/><br/>
     * @Since: 2024.03.30
     * @Author: 장세웅
     */
    @Override
    @Transactional
    public PickUploadImageResponse uploadImages(String name, List<MultipartFile> multipartFiles) {

        // 이미지 갯수 제한
        if (multipartFiles.size() > MAX_IMAGE_SIZE) {
            String exceptionMessage = String.format(INVALID_PICK_OPTION_IMAGE_SIZE_MESSAGE, MAX_IMAGE_SIZE);
            throw new ImageFileException(exceptionMessage);
        }

        // 픽픽픽은 2개의 옵션이 존재하고 각 옵션마다 이미지를 업로드 할 수 있다.
        if (!FIRST_PICK_OPTION_IMAGE.equals(name) && !SECOND_PICK_OPTION_IMAGE.equals(name)) {
            throw new PickOptionImageNameException(INVALID_PICK_OPTION_IMAGE_NAME_MESSAGE);
        }

        // 픽 옵션 이미지 업로드
        S3 s3 = awsS3Properties.getS3();
        List<S3ImageObject> imageObjects = awsS3Uploader.uploadMultipleImage(multipartFiles, s3.bucket(),
                s3.createPickPickPickDirectory());

        // 픽 옵션 이미지 생성
        List<PickOptionImage> pickOptionImage = imageObjects.stream()
                .map(image -> PickOptionImage.create(image.getImageUrl(), image.getKey(), name))
                .toList();

        // 픽 옵션 이미지 저장
        List<PickOptionImage> pickOptionImages = pickOptionImageRepository.saveAll(pickOptionImage);

        return PickUploadImageResponse.from(pickOptionImages);
    }

    /**
     * @Note: 픽픽픽 픽 옵션에 대한 이미지를 삭제 합니다.<br/>
     * @Author: 장세웅
     * @Since: 2024.03.30
     */
    @Override
    @Transactional
    public void deleteImage(Long pickOptionImageId) {
        // 픽 옵션 이미지 조회
        PickOptionImage findPickOptionImage = pickOptionImageRepository.findById(pickOptionImageId)
                .orElseThrow(() -> new ImageFileException(INVALID_NOT_FOUND_PICK_OPTION_IMAGE_MESSAGE));

        // 이미지 삭제
        S3 s3 = awsS3Properties.getS3();
        String imageKey = findPickOptionImage.getImageKey();
        awsS3Uploader.deleteSingleImage(s3.bucket(), imageKey);

        // 픽 옵션 이미지 삭제
        pickOptionImageRepository.deleteById(pickOptionImageId);
    }

    /**
     * 픽픽픽 작성
     */
    @Override
    @Transactional
    public PickRegisterResponse registerPick(RegisterPickRequest registerPickRequest, Authentication authentication) {

        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 픽픽픽 생성 및 저장
        String pickTitle = registerPickRequest.getPickTitle();
        Pick pick = Pick.create(new Title(pickTitle), member.getName(), member);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성 및 저장
        Map<PickOptionType, RegisterPickOptionRequest> pickOptions = registerPickRequest.getPickOptions();
        savePickOptionWithPickOptionImages(pick, pickOptions);

        return new PickRegisterResponse(pick.getId());
    }

    /**
     * 픽픽픽 수정
     */
    @Override
    @Transactional
    public PickModifyResponse modifyPick(Long pickId, ModifyPickRequest modifyPickRequest,
                                         Authentication authentication) {

        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 픽픽픽 조회
        Pick findPick = pickRepository.findPickWithPickOptionWithPickVoteWithMemberByPickId(pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_CAN_MODIFY_PICK_MESSAGE));

        // 회원이 작성한 픽픽픽이 아닌 경우
        if (!findPick.isEqualMember(member)) {
            throw new AccessDeniedException(INVALID_MODIFY_MEMBER_PICK_ONLY_MESSAGE);
        }

        // 픽픽픽 타이틀 수정
        findPick.changeTitle(modifyPickRequest.getPickTitle());

        // 픽픽픽 옵션1,2 수정 / 픽픽픽 옵션 관련 연관관계 설정
        List<PickOption> pickOptions = findPick.getPickOptions();
        Map<PickOptionType, ModifyPickOptionRequest> modifyPickRequestPickOptions = modifyPickRequest.getPickOptions();

        changePickOptionAndPickOptionImages(modifyPickRequestPickOptions, pickOptions);

        return new PickModifyResponse(findPick.getId());
    }

    /**
     * 픽픽픽 상세 조회
     */
    @Transactional
    @Override
    public PickDetailResponse findPickDetail(Long pickId, String anonymousMemberId, Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

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
                        pickOption -> PickDetailOptionResponse.of(pickOption, findPick, findMember))
                );

        // 픽픽픽 상세
        return PickDetailResponse.of(findPick, findPick.getMember(), findMember, pickDetailOptions);
    }

    /**
     * @Note: member 1:N pick 1:N pickOption 1:N pickVote <br/> pick 1:N pickVote N:1 member <br/> 연관관계가 다소 복잡하니, 직접
     * ERD를 확인하는 것을 권장합니다. <br/> 투표 이력이 있는 경우 - 투표 이력이 없는 경우
     * @Author: ralph
     * @Since: 2024.05.29
     */
    @Transactional
    @Override
    public VotePickResponse votePickOption(VotePickOptionDto votePickOptionDto,
                                           Authentication authentication) {

        Long pickId = votePickOptionDto.getPickId();
        Long pickOptionId = votePickOptionDto.getPickOptionId();

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 픽픽픽 투표 조회
        Optional<PickVote> pickVoteOptional = pickVoteRepository.findByPickIdAndMember(pickId,
                findMember);

        return pickVoteOptional
                .map(pickVote -> getVotePickResponseAndHandlePickVoteAndPickOptionExistingPickVoteOnPickOption(
                        pickVote, pickOptionId, pickId, findMember)
                )
                .orElseGet(() -> getVoteResponseAndHandlePickVoteAndPickOptionNotExistingPickVoteOnPickOption(
                        pickId, pickOptionId, findMember)
                );
    }

    // 픽픽픽 옵션에 투표 이력이 있는 경우
    private VotePickResponse getVotePickResponseAndHandlePickVoteAndPickOptionExistingPickVoteOnPickOption(
            PickVote pickVote, Long pickOptionId, Long pickId, Member member) {

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
                .map(findPickOption -> getVotePickOptionResponseAndHandlePickVoteAndPickOptionExistingPickVoteOnPickOption(
                        findPickOption, pickOptionId, findPick, member, pickVote))
                .collect(Collectors.toList());

        // 인기 점수 계산
        findPick.changePopularScore(pickPopularScorePolicy);

        return VotePickResponse.of(findPick.getId(), votePickOptionsResponse);
    }

    // 픽픽픽 옵션에 투표 이력이 없는 경우
    private VotePickResponse getVoteResponseAndHandlePickVoteAndPickOptionNotExistingPickVoteOnPickOption(Long pickId,
                                                                                                          Long pickOptionId,
                                                                                                          Member member) {
        // 픽픽픽 조회
        Pick findPick = pickRepository.findPickWithPickOptionByPickId(pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_MESSAGE));

        // 픽픽픽 전체 득표수 증가
        findPick.plusOneVoteTotalCount();

        // 데이터 가공 및 로직 수행
        List<VotePickOptionResponse> votePickOptionResponses = findPick.getPickOptions().stream()
                .map(findPickOption -> getVotePickOptionResponseAndHandlePickVoteAndPickOptionNotExistingPickVoteOnPickOption(
                        findPickOption, pickOptionId, findPick, member))
                .toList();

        // 인기 점수 계산
        findPick.changePopularScore(pickPopularScorePolicy);

        return VotePickResponse.of(findPick.getId(), votePickOptionResponses);
    }

    // 픽픽픽 옵션에 투표 이력이 없는 경우
    private VotePickOptionResponse getVotePickOptionResponseAndHandlePickVoteAndPickOptionNotExistingPickVoteOnPickOption(
            PickOption findPickOption, Long pickOptionId, Pick findPick, Member member) {

        // 투표를 하고 싶은 픽픽픽 옵션과 일치하면
        if (findPickOption.isEqualsId(pickOptionId)) {

            // 투표 생성
            PickVote newPickVote = PickVote.createByMember(member, findPickOption.getPick(),
                    findPickOption);
            pickVoteRepository.save(newPickVote);

            // 투표수 증가
            findPickOption.plusOneVoteTotalCount();

            // 득표율 계산
            BigDecimal percent = PickOption.calculatePercentBy(findPick, findPickOption);

            return VotePickOptionResponse.of(findPickOption, newPickVote.getId(), percent, true);
        }

        // 득표율 계산
        BigDecimal percent = PickOption.calculatePercentBy(findPick, findPickOption);

        return VotePickOptionResponse.of(findPickOption, null, percent, false);
    }


    // 다른 픽픽픽 옵션에 투표 했을 경우
    private VotePickOptionResponse getVotePickOptionResponseAndHandlePickVoteAndPickOptionExistingPickVoteOnPickOption(
            PickOption findPickOption, Long pickOptionId, Pick findPick, Member member,
            PickVote pickVote) {

        // 투표를 하고 싶은 픽픽픽 옵션과 일치하면
        if (findPickOption.isEqualsId(pickOptionId)) {
            // 픽픽픽 옵션 투표수 증가
            findPickOption.plusOneVoteTotalCount();

            // 투표 생성
            PickVote newPickVote = PickVote.createByMember(member, findPick, findPickOption);
            pickVoteRepository.save(newPickVote);

            // 득표율 계산
            BigDecimal percent = PickOption.calculatePercentBy(findPick, findPickOption);

            return VotePickOptionResponse.of(findPickOption, newPickVote.getId(), percent, true);
        }

        // 기존 픽픽픽 옵션 투표수 감소
        findPickOption.minusVoteTotalCount();

        // 투표 삭제
        pickVoteRepository.delete(pickVote);

        // 득표율 계산
        BigDecimal percent = PickOption.calculatePercentBy(findPick, findPickOption);

        return VotePickOptionResponse.of(findPickOption, null, percent, false);
    }

    /**
     * 픽픽픽 삭제 픽픽픽 외래키가 있는 모든 엔티티도 함께 삭제해야 한다.
     */
    @Transactional
    @Override
    public void deletePick(Long pickId, Authentication authentication) {

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 회원이 작성한 픽픽픽 조회(PickOption 페치 조인)
        Pick findPick = pickRepository.findPickWithPickOptionByIdAndMember(pickId, findMember)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_MESSAGE));

        List<Long> findPickPickOptionIds = findPick.getPickOptions().stream()
                .map(PickOption::getId).toList();

        // 존재하면 픽픽픽과 연관되어 있는 엔티티 삭제
        pickVoteRepository.deleteAllByPickOptionIn(findPickPickOptionIds);
        pickOptionImageRepository.deleteAllByPickOptionIn(findPickPickOptionIds);
        pickOptionRepository.deleteAllByPickOptionIdIn(findPickPickOptionIds);

        // 픽픽픽 삭제
        pickRepository.deleteById(findPick.getId());
    }

    private VotePickOptionResponse getVotePickOptionResponse(PickOption pickOption, Pick findPick, Member findMember,
                                                             Long pickOptionId) {

        // 해당 픽 옵션에 투표한 경우
        if (pickOption.isEqualsId(pickOptionId)) {
            return getPickedTrueVotePickOptionResponse(pickOption, findPick, findMember);
        }

        // 해당 픽 옵션에 투표하지 않은 경우
        return getPickedFalseVotePickOptionResponse(pickOption, findPick);
    }

    private VotePickOptionResponse getPickedFalseVotePickOptionResponse(PickOption pickOption, Pick findPick) {
        pickOption.minusVoteTotalCount(); // 득표 수 감소
        BigDecimal percent = PickOption.calculatePercentBy(findPick, pickOption);// 득표율 계산

        return VotePickOptionResponse.of(pickOption, null, percent, false);
    }

    private VotePickOptionResponse getPickedTrueVotePickOptionResponse(PickOption pickOption, Pick findPick,
                                                                       Member findMember) {
        pickOption.plusOneVoteTotalCount(); // 득표 수 증가
        findPick.plusOneVoteTotalCount(); // 득표 수 증가
        BigDecimal percent = PickOption.calculatePercentBy(findPick, pickOption);// 득표율 계산

        // 투표 생성
        PickVote pickVote = PickVote.createByMember(findMember, findPick, pickOption);
        pickVoteRepository.save(pickVote);

        return VotePickOptionResponse.of(pickOption, pickVote.getId(), percent, true);
    }

    private void changePickOptionAndPickOptionImages(
            Map<PickOptionType, ModifyPickOptionRequest> modifyPickRequestPickOptions, List<PickOption> pickOptions) {

        modifyPickRequestPickOptions.forEach((key, value) -> {
            List<Long> pickOptionImageIds = modifyPickRequestPickOptions.get(key).getPickOptionImageIds();
            List<PickOptionImage> pickOptionImages = getPickOptionImagesOrEmptyList(pickOptionImageIds);
            pickOptions.stream()
                    .filter(pickOption -> pickOption.isEqualsId(value.getPickOptionId()))
                    .forEach(pickOption -> {
                        pickOption.changePickOption(value);
                        pickOption.changePickOptionImages(pickOptionImages);
                    });
        });
    }

    // 픽옵션 이미지가 없을 수도 있다.
    private void savePickOptionWithPickOptionImages(Pick pick,
                                                    Map<PickOptionType, RegisterPickOptionRequest> registerPickOptionsRequest) {

        registerPickOptionsRequest.forEach((key, value) -> {
            RegisterPickOptionRequest registerPickOptionRequest = registerPickOptionsRequest.get(key);
            List<Long> pickOptionImageIds = registerPickOptionRequest.getPickOptionImageIds();

            List<PickOptionImage> findPickOptionImages = getPickOptionImagesOrEmptyList(pickOptionImageIds);

            String pickOptionTitle = registerPickOptionRequest.getPickOptionTitle();
            String pickOptionContent = registerPickOptionRequest.getPickOptionContent();
            PickOption pickOption = PickOption.create(new Title(pickOptionTitle),
                    new PickOptionContents(pickOptionContent), key,
                    findPickOptionImages, pick);

            pickOptionRepository.save(pickOption);
        });
    }

    // 픽 옵션 이미지 아이디가 없으면 select 할 필요가 없음
    private List<PickOptionImage> getPickOptionImagesOrEmptyList(List<Long> pickOptionImageIds) {
        if (!ObjectUtils.isEmpty(pickOptionImageIds)) {
            List<PickOptionImage> findPickOptionImages = pickOptionImageRepository.findByIdIn(pickOptionImageIds);
            return getPickOptionImages(findPickOptionImages);
        }
        return Collections.emptyList();
    }

    private List<PickOptionImage> getPickOptionImages(List<PickOptionImage> findPickOptionImages) {
        if (ObjectUtils.isEmpty(findPickOptionImages)) {
            throw new NotFoundException(PickExceptionMessage.INVALID_NOT_FOUND_PICK_OPTION_IMAGE_MESSAGE);
        }
        return findPickOptionImages;
    }
}
