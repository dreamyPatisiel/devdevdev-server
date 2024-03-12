package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.aws.s3.AwsS3Uploader;
import com.dreamypatisiel.devdevdev.aws.s3.S3ImageObject;
import com.dreamypatisiel.devdevdev.aws.s3.properties.AwsS3Properties;
import com.dreamypatisiel.devdevdev.aws.s3.properties.S3;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.repository.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.response.PickOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PicksResponse;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.utils.FileUtils;
import com.dreamypatisiel.devdevdev.web.controller.request.PickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterPickRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
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
    public static final int INIT_TOTAL_VOTE_COUNT = 0;
    public static final int INIT_TOTAL_VIEW_COUNT = 0;
    public static final int INIT_TOTAL_COMMENT_COUNT = 0;

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

    @Deprecated
    @Override
    @Transactional
    public Long registerPick(RegisterPickRequest registerPickRequest,
                             Map<String, List<MultipartFile>> registerPickImageFiles,
                             Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 2개의 픽옵션에 대한 이미지 파일들
        List<MultipartFile> firstPickOptionImage = registerPickImageFiles.get(FIRST_PICK_OPTION_IMAGE);
        List<MultipartFile> secondPickOptionImage = registerPickImageFiles.get(SECOND_PICK_OPTION_IMAGE);

        // 각 픽옵션에 대한 이미지 파일들 업로드
        S3 s3 = awsS3Properties.getS3();
        List<S3ImageObject> firstS3ImageObjects = awsS3Uploader.uploads(firstPickOptionImage, s3.bucket(), s3.createPickPickPickDirectory());
        List<S3ImageObject> secondS3ImageObjects = awsS3Uploader.uploads(secondPickOptionImage, s3.bucket(), s3.createPickPickPickDirectory());

        // 1개의 픽옵션에 N개의 이미지 존재(현재 픽픽픽 1개당 2개의 픽옵션이 존재하고 1개의 픽옵션에 N개의 사진 존재)
        List<PickOptionRequest> pickOptionsRequest = registerPickRequest.getPickOptions();
        List<PickOption> pickOptions = IntStream.range(START_INCLUSIVE, pickOptionsRequest.size())
                .mapToObj(pickOptionIndex -> {
                    List<S3ImageObject> s3ImageObjects = pickOptionIndex == START_INCLUSIVE ? firstS3ImageObjects : secondS3ImageObjects;

                    List<PickOptionImage> pickOptionImages = s3ImageObjects.stream()
                            .map(imageObject -> PickOptionImage.create(
                                    imageObject.imageUrl(), imageObject.key()
                            ))
                            .toList();

                    pickOptionImageRepository.saveAll(pickOptionImages);

                    PickOptionRequest pickOptionRequest = pickOptionsRequest.get(pickOptionIndex);

                    return PickOption.create(
                            new Title(pickOptionRequest.getPickOptionTitle()),
                            new PickContents(pickOptionRequest.getPickOptionContent()),
                            new Count(INIT_TOTAL_VOTE_COUNT),
                            pickOptionImages
                    );
                })
                .toList();

        // 픽 생성
        String pickTitle = registerPickRequest.getPickTitle();
        Pick pick = Pick.createForRegisterPick(
                new Title(pickTitle), new Count(INIT_TOTAL_VOTE_COUNT), new Count(INIT_TOTAL_VIEW_COUNT),
                new Count(INIT_TOTAL_COMMENT_COUNT), member.getName(), member, pickOptions);

        // 픽 저장
        pickRepository.save(pick);
        pickOptionRepository.saveAll(pickOptions);

        return pick.getId();
    }

    @Override
    public Long registerPick(RegisterPickRequest registerPickRequest, Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 픽 이미지 조회(픽 이미지가 업로드 되어 있으면 pickImageId를 클라이언트가 요청)
        List<List<Long>> pick = registerPickRequest.getPickOptions().stream()
                .map(po -> po.getPickOptionImageId().stream()
                        .toList()
                )
                .toList();

        // 픽 옵션 생성
        List<Object> option = registerPickRequest.getPickOptions().stream()
                .map(po -> PickOption.create(new Title(po.getPickOptionTitle()),
                        new PickContents(po.getPickOptionContent()),
                        ))
                .toList();

        // 픽 생성
        String pickTitle = registerPickRequest.getPickTitle();
        Pick.create(new Title(pickTitle), member.getName(), member);


        return null;
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
