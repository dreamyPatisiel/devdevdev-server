package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.aws.s3.AwsS3Uploader;
import com.dreamypatisiel.devdevdev.aws.s3.S3ImageObject;
import com.dreamypatisiel.devdevdev.aws.s3.properties.AwsS3Properties;
import com.dreamypatisiel.devdevdev.aws.s3.properties.S3;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.repository.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.response.PickOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickUploadImageResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PicksResponse;
import com.dreamypatisiel.devdevdev.exception.ImageFileException;
import com.dreamypatisiel.devdevdev.exception.PickOptionImageNameException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;

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
    public static final String INVALID_PICK_OPTION_IMAGE_NAME_MESSAGE = "픽픽픽 이미지에 알맞지 않은 형식의 이름 입니다.";
    public static final String INVALID_NOT_FOUND_PICK_OPTION_IMAGE_MESSAGE = "이미지가 존재하지 않습니다.";

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
        List<PicksResponse> picksResponses = picks.stream()
                .map(pick -> mapToPickResponse(pick, member))
                .toList();

        return new SliceImpl<>(picksResponses, pageable, picks.hasNext());
    }

    /**
     * @Note:
     * 이미지 업로드와 DB 저장을 하나의 작업(Transcation)으로 묶어서, 데이터 정합성을 유지한다.<br/>
     * 이때 pick_option_id는 null 인 상태 입니다.<br/><br/>
     *
     * 이미지 업로드 실패시 IOException이 발생할 수 있는데, 이때 catch로 처리하여 데이터 정합성 유지합니다.<br/>
     * 즉, IOException이 발생해도 rollback하지 않는다는 의미 입니다. <br/><br/>
     *
     * 단, Transcation이 길게 유지되면 추후 DB Connection을 오랫동안 유지하기 때문에
     * 많은 트래픽이 발생할 때 DB Connection이 부족해지는 현상이 발생할 수 있습니다.<br/><br/>
     *
     * (Transcation은 기본적으로 RuntimeException에 대해서만 Rollback 합니다.
     * AmazonClient의 putObject(...)는 RuntimeException을 발생시킵니다.)<br/><br/>
     *
     * @Since: 2024.03.30
     * @Author: 장세웅
     */
    @Override
    @Transactional
    public PickUploadImageResponse uploadImages(String name, List<MultipartFile> multipartFiles) {

        // 픽픽픽은 2개의 옵션이 존재하고 각 옵션마다 이미지를 업로드 할 수 있다.
        if(!FIRST_PICK_OPTION_IMAGE.equals(name) && !SECOND_PICK_OPTION_IMAGE.equals(name)) {
            throw new PickOptionImageNameException(INVALID_PICK_OPTION_IMAGE_NAME_MESSAGE);
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
     * @Note:
     * 픽픽픽 픽 옵션에 대한 이미지를 삭제 합니다.<br/><br/>
     *
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
