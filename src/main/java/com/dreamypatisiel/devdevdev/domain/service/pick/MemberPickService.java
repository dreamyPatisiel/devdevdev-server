package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.web.controller.request.PickOptionName.*;

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
import com.dreamypatisiel.devdevdev.domain.repository.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.response.PickOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickRegisterResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickUploadImageResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PicksResponse;
import com.dreamypatisiel.devdevdev.exception.PickOptionImageNameException;
import com.dreamypatisiel.devdevdev.exception.PickOptionNameException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.web.controller.request.PickOptionName;
import com.dreamypatisiel.devdevdev.web.controller.request.PickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.PickRegisterRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPickService implements PickService {

    public static final String FIRST_PICK_OPTION_IMAGE = "firstPickOptionImage";
    public static final String SECOND_PICK_OPTION_IMAGE = "secondPickOptionImage";
    public static final int START_INCLUSIVE = 0;
    public static final String INVALID_PICK_IMAGE_NAME_MESSAGE = "픽픽픽 이미지에 알맞지 않은 형식의 이름 입니다.";
    public static final String INVALID_PICK_OPTION_NAME_MESSAGE = "잘못된 형식의 픽픽픽 선택지 입니다.";

    private final AwsS3Properties awsS3Properties;
    private final AwsS3Uploader awsS3Uploader;
    private final MemberProvider memberProvider;
    private final PickRepository pickRepository;
    private final PickOptionRepository pickOptionRepository;
    private final PickOptionImageRepository pickOptionImageRepository;

    @Override
    public Slice<PicksResponse> findPicksMain(Pageable pageable, Long pickId, PickSort pickSort, Authentication authentication) {
        // 픽픽픽 조회
        Slice<Pick> picks = pickRepository.findPicksByCursor(pageable, pickId, pickSort);

        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 데이터 가공
        List<PicksResponse> picksResponse = picks.stream()
                .map(pick -> mapToPickResponse(pick, member))
                .toList();

        return new SliceImpl<>(picksResponse, pageable, picks.hasNext());
    }

    /**
     * 이미지 업로드와 DB 저장을 하나의 작업(Transcation)으로 묶어서, 데이터 정합성을 유지한다.
     */
    @Override
    @Transactional
    public PickUploadImageResponse uploadImages(String name, List<MultipartFile> multipartFiles) {

        // 픽픽픽은 2개의 옵션이 존재하고 각 옵션마다 이미지를 업로드 할 수 있다.
        if(!FIRST_PICK_OPTION_IMAGE.equals(name) && !SECOND_PICK_OPTION_IMAGE.equals(name)) {
            throw new PickOptionImageNameException(INVALID_PICK_IMAGE_NAME_MESSAGE);
        }

        // 픽 옵션 이미지 업로드
        S3 s3 = awsS3Properties.getS3();
        List<S3ImageObject> imageObjects = awsS3Uploader.uploadMultipleImage(multipartFiles, s3.bucket(), s3.createPickPickPickDirectory());

        // 픽 옵션 이미지 생성
        List<PickOptionImage> pickOptionImage = imageObjects.stream()
                .map(image -> PickOptionImage.create(image.getImageUrl(), image.getKey(), name))
                .toList();

        // 픽 옵션 이미지 저장
        List<PickOptionImage> pickOptionImages = pickOptionImageRepository.saveAll(pickOptionImage);

        return PickUploadImageResponse.from(pickOptionImages);
    }

    /**
     * 픽픽픽 작성
     */
    @Override
    @Transactional
    public PickRegisterResponse registerPick(PickRegisterRequest pickRegisterRequest, Authentication authentication) {

        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 픽픽픽 생성 및 저장
        String pickTitle = pickRegisterRequest.getPickTitle();
        Pick pick = Pick.create(new Title(pickTitle), member.getName(), member);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성 및 저장
        savePickOptionWithPickOptionImages(FIRST_PICK_OPTION, pick, pickRegisterRequest);
        savePickOptionWithPickOptionImages(SECOND_PICK_OPTION, pick, pickRegisterRequest);

        return new PickRegisterResponse(pick.getId());
    }

    private void savePickOptionWithPickOptionImages(PickOptionName pickOptionName, Pick pick, PickRegisterRequest pickRegisterRequest) {

        // firstPickOption 또는 secondPickOption key 값이 아니면
        String pickOptionNameDescription = pickOptionName.getDescription();
        if(!pickRegisterRequest.getPickOptions().containsKey(pickOptionNameDescription)) {
            throw new PickOptionNameException(INVALID_PICK_OPTION_NAME_MESSAGE);
        }

        // 픽픽픽 이미지 조회에 필요한 픽픽픽 옵션 이미지 아이디 목록 꺼내기
        PickOptionRequest pickOptionRequest = pickRegisterRequest.getPickOptions().get(pickOptionNameDescription);
        List<Long> pickOptionImageIds = pickOptionRequest.getPickOptionImageIds();

        // 픽픽픽 이미지 조회
        List<PickOptionImage> findPickOptionImages = pickOptionImageRepository.findByIdIn(pickOptionImageIds);

        // 픽픽픽 옵션 생성
        String pickOptionTitle = pickOptionRequest.getPickOptionTitle();
        String pickOptionContent = pickOptionRequest.getPickOptionContent();
        PickOption pickOption = PickOption.create(new Title(pickOptionTitle), new PickOptionContents(pickOptionContent),
                findPickOptionImages, pick);

        // 픽픽픽 옵션 저장
        pickOptionRepository.save(pickOption);
    }

    private PicksResponse mapToPickResponse(Pick pick, Member member) {
        return PicksResponse.builder()
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
                .anyMatch(pickVote -> pickVote.getMember().isEqualsMember(member));
    }

    private List<PickOptionResponse> mapToPickOptionsResponse(Pick pick, Member member) {
        return pick.getPickOptions().stream()
                .map(pickOption -> mapToPickOptionResponse(pick, pickOption, member))
                .toList();
    }

    private PickOptionResponse mapToPickOptionResponse(Pick pick, PickOption pickOption, Member member) {
        return PickOptionResponse.builder()
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
