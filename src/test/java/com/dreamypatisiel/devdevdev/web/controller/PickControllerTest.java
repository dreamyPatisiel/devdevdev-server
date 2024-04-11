package com.dreamypatisiel.devdevdev.web.controller;

import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickService.FIRST_PICK_OPTION_IMAGE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickService.SECOND_PICK_OPTION_IMAGE;
import static com.dreamypatisiel.devdevdev.web.controller.request.PickOptionName.FIRST_PICK_OPTION;
import static com.dreamypatisiel.devdevdev.web.controller.request.PickOptionName.SECOND_PICK_OPTION;
import static com.dreamypatisiel.devdevdev.web.response.ResultType.SUCCESS;
import static io.lettuce.core.BitFieldArgs.OverflowType.FAIL;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.response.PickModifyResponse;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.controller.request.ModifyPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterPickRequest;
import com.dreamypatisiel.devdevdev.web.response.ResultType;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class PickControllerTest extends SupportControllerTest {

    @Autowired
    PickRepository pickRepository;
    @Autowired
    PickOptionRepository pickOptionRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PickPopularScorePolicy pickPopularScorePolicy;
    @Autowired
    PickOptionImageRepository pickOptionImageRepository;
    @MockBean
    AmazonS3 amazonS3Client;

    @Test
    @DisplayName("회원이 픽픽픽 메인을 조회한다.")
    void getPicksMainByMember() throws Exception {
        // given
        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"), new Count(1));
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"), new Count(1));
        Title title = new Title("픽1타이틀");
        Count count = new Count(2);
        String thumbnailUrl = "https://devdevdev.co.kr/devdevdev/api/v1/pick/image/1";
        String author = "운영자";
        Pick pick = createPick(title, count, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        pick.changePopularScore(pickPopularScorePolicy);

        pickRepository.save(pick);
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        Pageable pageable = PageRequest.of(0, 10);

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/picks")
                .queryParam("size", String.valueOf(pageable.getPageSize()))
                .queryParam("pickId", String.valueOf(Long.MAX_VALUE))
                .queryParam("pickSort", PickSort.LATEST.name())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].title").isString())
                .andExpect(jsonPath("$.data.content.[0].voteTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].commentTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].viewTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].popularScore").isNumber())
                .andExpect(jsonPath("$.data.content.[0].isVoted").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].pickOptions").isArray())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[0].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[0].title").isString())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[0].percent").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[0].isPicked").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[1].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[1].title").isString())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[1].percent").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[1].isPicked").isBoolean())
                .andExpect(jsonPath("$.data.pageable").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable.pageNumber").isNumber())
                .andExpect(jsonPath("$.data.pageable.pageSize").isNumber())
                .andExpect(jsonPath("$.data.pageable.sort").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable.sort.empty").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.sorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.unsorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.offset").isNumber())
                .andExpect(jsonPath("$.data.pageable.paged").isBoolean())
                .andExpect(jsonPath("$.data.pageable.unpaged").isBoolean())
                .andExpect(jsonPath("$.data.first").isBoolean())
                .andExpect(jsonPath("$.data.last").isBoolean())
                .andExpect(jsonPath("$.data.size").isNumber())
                .andExpect(jsonPath("$.data.number").isNumber())
                .andExpect(jsonPath("$.data.sort").isNotEmpty())
                .andExpect(jsonPath("$.data.sort.empty").isBoolean())
                .andExpect(jsonPath("$.data.sort.sorted").isBoolean())
                .andExpect(jsonPath("$.data.sort.unsorted").isBoolean())
                .andExpect(jsonPath("$.data.numberOfElements").isNumber())
                .andExpect(jsonPath("$.data.empty").isBoolean());
    }

    @Test
    @DisplayName("익명 사용자가 픽픽픽 메인을 조회한다.")
    void getPicksMainByAnonymous() throws Exception {
        // given
        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"), new Count(1));
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"), new Count(1));
        Title title = new Title("픽1타이틀");
        Count count = new Count(2);
        String thumbnailUrl = "https://devdevdev.co.kr/devdevdev/api/v1/pick/image/1";
        String author = "운영자";
        Pick pick = createPick(title, count, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        pick.changePopularScore(pickPopularScorePolicy);

        pickRepository.save(pick);
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/picks")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("pickId", String.valueOf(Long.MAX_VALUE))
                        .queryParam("pickSort", PickSort.LATEST.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].title").isString())
                .andExpect(jsonPath("$.data.content.[0].voteTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].commentTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions").isArray())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[0].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[0].title").isString())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[0].percent").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[1].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[1].title").isString())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[1].percent").isNumber())
                .andExpect(jsonPath("$.data.pageable").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable.pageNumber").isNumber())
                .andExpect(jsonPath("$.data.pageable.pageSize").isNumber())
                .andExpect(jsonPath("$.data.pageable.sort").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable.sort.empty").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.sorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.unsorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.offset").isNumber())
                .andExpect(jsonPath("$.data.pageable.paged").isBoolean())
                .andExpect(jsonPath("$.data.pageable.unpaged").isBoolean())
                .andExpect(jsonPath("$.data.first").isBoolean())
                .andExpect(jsonPath("$.data.last").isBoolean())
                .andExpect(jsonPath("$.data.size").isNumber())
                .andExpect(jsonPath("$.data.number").isNumber())
                .andExpect(jsonPath("$.data.sort").isNotEmpty())
                .andExpect(jsonPath("$.data.sort.empty").isBoolean())
                .andExpect(jsonPath("$.data.sort.sorted").isBoolean())
                .andExpect(jsonPath("$.data.sort.unsorted").isBoolean())
                .andExpect(jsonPath("$.data.numberOfElements").isNumber())
                .andExpect(jsonPath("$.data.empty").isBoolean());
    }

    @Test
    @DisplayName("회원만 픽픽픽 이미지를 업로드 할 수 있다.")
    void uploadPickOptionImages() throws Exception {
        // given
        MockMultipartFile mockMultipartFile = createMockMultipartFile("pickOptionImages", "tesImage.png");
        String bucket = "bucket";
        String key = "/pick/pickOption/image/xxx.png";

        ObjectMetadata objectMetadata = createObjectMetadataByMultipartFile(mockMultipartFile);
        PutObjectRequest putObjectRequest = createPutObjectRequest(bucket, key, mockMultipartFile, objectMetadata);

        // when
        PutObjectResult putObjectResult = mock(PutObjectResult.class);

        when(amazonS3Client.putObject(eq(putObjectRequest))).thenReturn(putObjectResult);
        when(amazonS3Client.getUrl(anyString(), anyString())).thenReturn(new URL("http", "localhost", 8080, "/xxx.png"));

        // then
        mockMvc.perform(multipart(HttpMethod.POST, "/devdevdev/api/v1/picks/image")
                        .file(mockMultipartFile)
                        .queryParam("name", FIRST_PICK_OPTION_IMAGE)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
                .andExpect(jsonPath("$.data.pickOptionImages").isArray())
                .andExpect(jsonPath("$.data.pickOptionImages[0].name").value(FIRST_PICK_OPTION_IMAGE))
                .andExpect(jsonPath("$.data.pickOptionImages[0].pickOptionImageId").isNumber())
                .andExpect(jsonPath("$.data.pickOptionImages[0].imageUrl").value("http://localhost:8080/xxx.png"))
                .andExpect(jsonPath("$.data.pickOptionImages[0].imageKey").isString());
    }

    /**
     * mockMvc가 yml에 설정을 반영하지 못하는 것 같음..
     */
    @Disabled
    @Test
    @DisplayName("픽픽픽 이미지를 업로드 할때 10MB 이상의 파일을 업로드하면 예외가 발생한다.")
    void uploadPickOptionImagesMaxUploadSizeExceededException() throws Exception {
        // given
        MockMultipartFile upper10MbMockMultipartFile = new MockMultipartFile("pickOptionImages",
                new FileInputStream("src/test/resources/testImage/10mb_image.jpg"));

        String bucket = "bucket";
        String key = "/pick/pickOption/image/xxx.png";

        ObjectMetadata objectMetadata = createObjectMetadataByMultipartFile(upper10MbMockMultipartFile);
        PutObjectRequest putObjectRequest = createPutObjectRequest(bucket, key, upper10MbMockMultipartFile, objectMetadata);

        // when
        PutObjectResult putObjectResult = mock(PutObjectResult.class);

        when(amazonS3Client.putObject(eq(putObjectRequest))).thenReturn(putObjectResult);
        when(amazonS3Client.getUrl(anyString(), anyString())).thenReturn(new URL("http", "localhost", 8080, "/xxx.png"));

        // then
        mockMvc.perform(multipart(HttpMethod.POST, "/devdevdev/api/v1/picks/image")
                        .file(upper10MbMockMultipartFile)
                        .queryParam("name", FIRST_PICK_OPTION_IMAGE)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").isNumber());
    }

    @Test
    @DisplayName("회원은 픽픽픽 옵션에 대한 이미지를 삭제할 수 있다.")
    void deletePickImage() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        MockMultipartFile mockMultipartFile = createMockMultipartFile("pickOptionImages", "tesImage.png");
        String bucket = "bucket";
        String key = "/pick/pickOption/image/xxx.png";

        ObjectMetadata objectMetadata = createObjectMetadataByMultipartFile(mockMultipartFile);
        PutObjectRequest putObjectRequest = createPutObjectRequest(bucket, key, mockMultipartFile, objectMetadata);

        URL url = new URL("http", "localhost", 8080, "/xxx.png");

        PickOptionImage pickOptionImage = createPickOptionImage(url.toString(), key);
        pickOptionImageRepository.save(pickOptionImage);

        // when
        PutObjectResult putObjectResult = mock(PutObjectResult.class);

        when(amazonS3Client.putObject(eq(putObjectRequest))).thenReturn(putObjectResult);
        when(amazonS3Client.getUrl(anyString(), anyString())).thenReturn(url);

        // then
        mockMvc.perform(delete("/devdevdev/api/v1/picks/image/{pickOptionImageId}"
                        , pickOptionImage.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()));
    }

    @Test
    @DisplayName("비회원은 픽픽픽 옵션에 대한 이미지를 삭제할 수 없다.")
    void deleteImageAnonymousException() throws Exception{
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        MockMultipartFile mockMultipartFile = createMockMultipartFile("pickOptionImages", "tesImage.png");
        String bucket = "bucket";
        String key = "/pick/pickOption/image/xxx.png";

        ObjectMetadata objectMetadata = createObjectMetadataByMultipartFile(mockMultipartFile);
        PutObjectRequest putObjectRequest = createPutObjectRequest(bucket, key, mockMultipartFile, objectMetadata);

        URL url = new URL("http", "localhost", 8080, "/xxx.png");

        PickOptionImage pickOptionImage = createPickOptionImage(url.toString(), key);
        pickOptionImageRepository.save(pickOptionImage);

        // when
        PutObjectResult putObjectResult = mock(PutObjectResult.class);

        when(amazonS3Client.putObject(eq(putObjectRequest))).thenReturn(putObjectResult);
        when(amazonS3Client.getUrl(anyString(), anyString())).thenReturn(url);

        // then
        mockMvc.perform(delete("/devdevdev/api/v1/picks/image/{pickOptionImageId}"
                        , pickOptionImage.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultType").value(FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").isNumber());
    }

    @Test
    @DisplayName("회원만 픽픽픽을 작성할 수 있다.")
    void registerPicks() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        String firstImageUrl = "http://devdevdev.co.kr/pickpickpick/fist.jpg";
        String secondImageUrl = "http://devdevdev.co.kr/pickpickpick/second.jpg";
        String imageKey = "/pickpickpick/xxx.jpg";

        PickOptionImage firstPickOptionImage = createPickOptionImage(FIRST_PICK_OPTION_IMAGE, firstImageUrl, imageKey);
        PickOptionImage secondPickOptionImage = createPickOptionImage(SECOND_PICK_OPTION_IMAGE, secondImageUrl, imageKey);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        RegisterPickOptionRequest firstRegisterPickOptionRequest = createPickOptionRequest("픽옵션1", "픽옵션1블라블라",
                List.of(firstPickOptionImage.getId()));
        RegisterPickOptionRequest secondRegisterPickOptionRequest = createPickOptionRequest("픽옵션2", "픽옵션2블라블라",
                List.of(secondPickOptionImage.getId()));

        Map<String, RegisterPickOptionRequest> pickOptions = new HashMap<>();
        pickOptions.put(FIRST_PICK_OPTION.getDescription(), firstRegisterPickOptionRequest);
        pickOptions.put(SECOND_PICK_OPTION.getDescription(), secondRegisterPickOptionRequest);

        RegisterPickRequest registerPickRequest = createPickRegisterRequest("나의 픽픽픽", pickOptions);

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/picks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(registerPickRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
                .andExpect(jsonPath("$.data.pickId").isNumber());
    }

    @Test
    @DisplayName("비회원은 픽픽픽 작성을 할 수 없다.")
    void registerPicksException() throws Exception {
        // given
        String firstImageUrl = "http://devdevdev.co.kr/pickpickpick/fist.jpg";
        String secondImageUrl = "http://devdevdev.co.kr/pickpickpick/second.jpg";
        String imageKey = "/pickpickpick/xxx.jpg";

        PickOptionImage firstPickOptionImage = createPickOptionImage(FIRST_PICK_OPTION_IMAGE, firstImageUrl, imageKey);
        PickOptionImage secondPickOptionImage = createPickOptionImage(SECOND_PICK_OPTION_IMAGE, secondImageUrl, imageKey);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        RegisterPickOptionRequest firstRegisterPickOptionRequest = createPickOptionRequest("픽옵션1", "픽옵션1블라블라",
                List.of(firstPickOptionImage.getId()));
        RegisterPickOptionRequest secondRegisterPickOptionRequest = createPickOptionRequest("픽옵션2", "픽옵션2블라블라",
                List.of(secondPickOptionImage.getId()));

        Map<String, RegisterPickOptionRequest> pickOptions = new HashMap<>();
        pickOptions.put(FIRST_PICK_OPTION.getDescription(), firstRegisterPickOptionRequest);
        pickOptions.put(SECOND_PICK_OPTION.getDescription(), secondRegisterPickOptionRequest);

        RegisterPickRequest registerPickRequest = createPickRegisterRequest("나의 픽픽픽", pickOptions);

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/picks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(registerPickRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("이미 작성된 픽픽픽을 수정한다.")
    void modifyPick() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // == 픽픽픽 작성 환경 == //
        Pick pick = createPick(new Title("픽제목"), member);
        pickRepository.save(pick);

        PickOption pickOption1 = createPickOption(pick, new Title("픽옵션1제목"), new PickOptionContents("픽옵션1콘텐츠"));
        PickOption pickOption2 = createPickOption(pick, new Title("픽옵션2제목"), new PickOptionContents("픽옵션2콘텐츠"));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        PickOptionImage pickOption1Image1 = createPickOptionImage("픽옵션1사진1", pickOption1);
        PickOptionImage pickOption1Image2 = createPickOptionImage("픽옵션1사진2", pickOption1);
        PickOptionImage pickOption2Image1 = createPickOptionImage("픽옵션2사진1", pickOption2);
        pickOptionImageRepository.saveAll(List.of(pickOption1Image1, pickOption1Image2, pickOption2Image1));
        // == 픽픽픽 작성 환경 == //

        // == 픽픽픽 새로운 사진 업로드 환경 == //
        pickOptionImageRepository.deleteAllById(List.of(pickOption1Image1.getId(), pickOption1Image2.getId(),
                pickOption2Image1.getId()));

        PickOptionImage newPickOption1Image1 = createPickOptionImage("픽옵션1사진1수정");
        PickOptionImage newPickOption2Image1 = createPickOptionImage("픽옵션2사진1수정");
        pickOptionImageRepository.saveAll(List.of(newPickOption1Image1, newPickOption2Image1));
        // == 픽픽픽 새로운 사진 업로드 환경 == //

        ModifyPickOptionRequest modifyPickOptionRequest1 = new ModifyPickOptionRequest(pickOption1.getId(), "픽옵션1제목수정",
                "픽옵션1콘텐츠수정", List.of(newPickOption1Image1.getId()));
        ModifyPickOptionRequest modifyPickOptionRequest2 = new ModifyPickOptionRequest(pickOption2.getId(), "픽옵션2제목수정",
                "픽옵션2콘텐츠수정", List.of(newPickOption2Image1.getId()));

        Map<String, ModifyPickOptionRequest> modifyPickOptionRequests = new HashMap<>();
        modifyPickOptionRequests.put(FIRST_PICK_OPTION.getDescription(), modifyPickOptionRequest1);
        modifyPickOptionRequests.put(SECOND_PICK_OPTION.getDescription(), modifyPickOptionRequest2);

        ModifyPickRequest modifyPickRequest = createModifyPickRequest("픽타이틀수정", modifyPickOptionRequests);

        // when // then
        mockMvc.perform(patch("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(modifyPickRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data.pickId").isNumber());
    }

    private Pick createPick(Title title, Member member) {
        return Pick.builder()
                .title(title)
                .member(member)
                .build();
    }

    private PickOptionImage createPickOptionImage(String name) {
        return PickOptionImage.builder()
                .name(name)
                .build();
    }

    private PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private PickOptionImage createPickOptionImage(String name, PickOption pickOption) {
        PickOptionImage pickOptionImage = PickOptionImage.builder()
                .name(name)
                .build();

        pickOptionImage.changePickOption(pickOption);

        return pickOptionImage;
    }


    private ModifyPickRequest createModifyPickRequest(String pickTitle, Map<String, ModifyPickOptionRequest> modifyPickOptionRequests) {
        return ModifyPickRequest.builder()
                .pickTitle(pickTitle)
                .pickOptions(modifyPickOptionRequests)
                .build();
    }

    private PickOptionImage createPickOptionImage(String name, String imageUrl, String imageKey) {
        return PickOptionImage.builder()
                .name(name)
                .imageUrl(imageUrl)
                .imageKey(imageKey)
                .build();
    }

    private RegisterPickRequest createPickRegisterRequest(String pickTitle, Map<String, RegisterPickOptionRequest> pickOptions) {
        return RegisterPickRequest.builder()
                .pickTitle(pickTitle)
                .pickOptions(pickOptions)
                .build();
    }

    private RegisterPickOptionRequest createPickOptionRequest(String pickOptionTitle, String pickOptionContent, List<Long> pickOptionImageIds) {
        return RegisterPickOptionRequest.builder()
                .pickOptionTitle(pickOptionTitle)
                .pickOptionContent(pickOptionContent)
                .pickOptionImageIds(pickOptionImageIds)
                .build();
    }

    private PickOptionImage createPickOptionImage(String imageUrl, String imageKey) {
        return PickOptionImage.builder()
                .imageUrl(imageUrl)
                .imageKey(imageKey)
                .build();
    }

    private ObjectMetadata createObjectMetadataByMultipartFile(MultipartFile multipartFile) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());

        return objectMetadata;
    }

    private PutObjectRequest createPutObjectRequest(String bucket, String key, MultipartFile multipartFile,
                                                    ObjectMetadata objectMetadata) throws IOException {

        return new PutObjectRequest(bucket, key, multipartFile.getInputStream(), objectMetadata);
    }

    private MockMultipartFile createMockMultipartFile(String name, String originalFilename) {
        return new MockMultipartFile(
                name,
                originalFilename,
                MediaType.IMAGE_PNG_VALUE,
                name.getBytes()
        );
    }

    private SocialMemberDto createSocialDto(String userId, String name, String nickName, String password, String email, String socialType, String role) {
        return SocialMemberDto.builder()
                .userId(userId)
                .name(name)
                .nickname(nickName)
                .password(password)
                .email(email)
                .socialType(SocialType.valueOf(socialType))
                .role(Role.valueOf(role))
                .build();
    }

    private Pick createPick(Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                            Count pickcommentTotalCount, String thumbnailUrl, String author,
                            List<PickOption> pickOptions, List<PickVote> pickVotes
    ) {

        Pick pick = Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteTotalCount)
                .viewTotalCount(pickViewTotalCount)
                .commentTotalCount(pickcommentTotalCount)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .build();

        pick.changePickOptions(pickOptions);
        pick.changePickVote(pickVotes);

        return pick;
    }

    private PickOption createPickOption(Title title, PickOptionContents pickOptionContents, Count voteTotalCount) {
        return PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(voteTotalCount)
                .build();
    }
}