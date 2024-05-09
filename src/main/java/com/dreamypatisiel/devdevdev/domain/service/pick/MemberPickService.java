package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_MODIFY_MEMBER_PICK_ONLY_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_CAN_MODIFY_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_OPTION_IMAGE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_PICK_OPTION_IMAGE_NAME_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_PICK_OPTION_IMAGE_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_PICK_OPTION_IMAGE_SIZE_MESSAGE;

import com.dreamypatisiel.devdevdev.aws.s3.AwsS3Uploader;
import com.dreamypatisiel.devdevdev.aws.s3.S3ImageObject;
import com.dreamypatisiel.devdevdev.aws.s3.properties.AwsS3Properties;
import com.dreamypatisiel.devdevdev.aws.s3.properties.S3;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailOptionImage;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickMainOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickMainResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickModifyResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickRegisterResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickUploadImageResponse;
import com.dreamypatisiel.devdevdev.exception.ImageFileException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.PickOptionImageNameException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.web.controller.request.ModifyPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterPickRequest;
import java.util.Collections;
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
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPickService implements PickService {

    public static final String FIRST_PICK_OPTION_IMAGE = "firstPickOptionImage";
    public static final String SECOND_PICK_OPTION_IMAGE = "firstPickOptionImage";
    public static final int MAX_IMAGE_SIZE = 3;
    public static final int START_INCLUSIVE = 0;

    private final AwsS3Properties awsS3Properties;
    private final AwsS3Uploader awsS3Uploader;
    private final MemberProvider memberProvider;
    private final PickRepository pickRepository;
    private final PickOptionRepository pickOptionRepository;
    private final PickOptionImageRepository pickOptionImageRepository;

    /**
     * 픽픽픽 메인 조회
     */
    @Override
    public Slice<PickMainResponse> findPicksMain(Pageable pageable, Long pickId, PickSort pickSort,
                                                 Authentication authentication) {
        // 픽픽픽 조회
        Slice<Pick> picks = pickRepository.findPicksByCursor(pageable, pickId, pickSort);

        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 데이터 가공
        List<PickMainResponse> pickMainRespons = picks.stream()
                .map(pick -> mapToPickResponse(pick, member))
                .toList();

        return new SliceImpl<>(pickMainRespons, pageable, picks.hasNext());
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
        Pick findPick = pickRepository.findPickAndPickOptionByPickId(pickId)
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
    @Override
    public PickDetailResponse findPickDetail(Long pickId, Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 픽픽픽 상세 조회(pickOption 페치조인)
        Pick findPick = pickRepository.findPickDetailByPickId(pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_MESSAGE));

        // 픽픽픽 옵션 가공
        Map<PickOptionType, PickDetailOptionResponse> pickDetailOptions = findPick.getPickOptions().stream()
                .collect(Collectors.toMap(PickOption::getPickOptionType,
                        pickOption -> mapToPickDetailOptionsResponse(pickOption, findPick, member)));

        // 픽픽픽 상세
        return PickDetailResponse.of(findPick, findPick.getMember(), member, pickDetailOptions);
    }

    private PickDetailOptionResponse mapToPickDetailOptionsResponse(PickOption pickOption, Pick findPick,
                                                                    Member member) {
        return PickDetailOptionResponse.builder()
                .id(pickOption.getId())
                .title(pickOption.getTitle().getTitle())
                .isPicked(isPickedPickOptionByMember(findPick, pickOption, member))
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

    private void changePickOptionAndPickOptionImages(
            Map<PickOptionType, ModifyPickOptionRequest> modifyPickRequestPickOptions, List<PickOption> pickOptions) {

        modifyPickRequestPickOptions.forEach((key, value) -> {
            List<Long> pickOptionImageIds = modifyPickRequestPickOptions.get(key).getPickOptionImageIds();
            List<PickOptionImage> pickOptionImages = getPickOptionImagesOrEmptyList(pickOptionImageIds);
            pickOptions.stream()
                    .filter(pickOption -> pickOption.getId().equals(value.getPickOptionId()))
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
            throw new NotFoundException(INVALID_PICK_OPTION_IMAGE_NOT_FOUND_MESSAGE);
        }
        return findPickOptionImages;
    }

    private PickMainResponse mapToPickResponse(Pick pick, Member member) {
        return PickMainResponse.builder()
                .id(pick.getId())
                .title(pick.getTitle())
                .isVoted(isVotedByPickAndMember(pick, member))
                .voteTotalCount(pick.getVoteTotalCount())
                .commentTotalCount(pick.getCommentTotalCount())
                .viewTotalCount(pick.getViewTotalCount())
                .popularScore(pick.getPopularScore())
                .pickOptions(mapToPickOptionsResponse(pick, member))
                .build();
    }

    private boolean isVotedByPickAndMember(Pick pick, Member member) {
        return pick.getPickVotes().stream()
                .filter(pickVote -> pickVote.getPick().isEqualsPick(pick))
                .anyMatch(pickVote -> pickVote.getMember().isEqualMember(member));
    }

    private List<PickMainOptionResponse> mapToPickOptionsResponse(Pick pick, Member member) {
        return pick.getPickOptions().stream()
                .map(pickOption -> mapToPickOptionResponse(pick, pickOption, member))
                .toList();
    }

    private PickMainOptionResponse mapToPickOptionResponse(Pick pick, PickOption pickOption, Member member) {
        return PickMainOptionResponse.builder()
                .id(pickOption.getId())
                .title(pickOption.getTitle())
                .percent(PickOption.calculatePercentBy(pick, pickOption))
                .isPicked(isPickedPickOptionByMember(pick, pickOption, member))
                .build();
    }

    private Boolean isPickedPickOptionByMember(Pick pick, PickOption pickOption, Member member) {
        return pick.getPickVotes().stream()
                .filter(pickVote -> pickVote.getPickOption().equals(pickOption))
                .anyMatch(pickVote -> pickVote.getMember().equals(member));
    }
}
