package com.dreamypatisiel.devdevdev.web.controller.pick;

import static com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType.firstPickOption;
import static com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType.secondPickOption;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickService.FIRST_PICK_OPTION_IMAGE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickService.SECOND_PICK_OPTION_IMAGE;
import static com.dreamypatisiel.devdevdev.web.dto.response.ResultType.SUCCESS;
import static io.lettuce.core.BitFieldArgs.OverflowType.FAIL;
import static org.mockito.ArgumentMatchers.any;
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
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.openai.data.request.EmbeddingRequest;
import com.dreamypatisiel.devdevdev.openai.data.response.Embedding;
import com.dreamypatisiel.devdevdev.openai.data.response.OpenAIResponse;
import com.dreamypatisiel.devdevdev.openai.data.response.Usage;
import com.dreamypatisiel.devdevdev.web.controller.SupportControllerTest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.VotePickOptionRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
import jakarta.persistence.EntityManager;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
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
    @Autowired
    PickVoteRepository pickVoteRepository;
    @Autowired
    EntityManager em;
    @MockBean
    AmazonS3 amazonS3Client;


    @Test
    @DisplayName("회원이 픽픽픽 메인을 조회한다.")
    void getPicksMainByMember() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"), new Count(1),
                firstPickOption);
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"), new Count(1),
                secondPickOption);

        Title title = new Title("픽1타이틀");
        Count count = new Count(2);
        String thumbnailUrl = "https://devdevdev.co.kr/devdevdev/api/v1/pick/image/1";
        String author = "운영자";
        Pick pick = createPick(member, title, count, count, count, thumbnailUrl, author, ContentStatus.APPROVAL,
                List.of(pickOption1, pickOption2), List.of());
        pick.changePopularScore(pickPopularScorePolicy);

        pickRepository.save(pick);
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

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
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"), new Count(1),
                firstPickOption);
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"), new Count(1),
                secondPickOption);

        Title title = new Title("픽1타이틀");
        Count count = new Count(2);
        String thumbnailUrl = "https://devdevdev.co.kr/devdevdev/api/v1/pick/image/1";
        String author = "운영자";
        Pick pick = createPick(member, title, count, count, count,
                thumbnailUrl, author, ContentStatus.APPROVAL, List.of(pickOption1, pickOption2), List.of());
        pick.changePopularScore(pickPopularScorePolicy);

        pickRepository.save(pick);
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        String anonymousMemberId = "GA1.1.276672604.1715872960";

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/picks")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("pickId", String.valueOf(Long.MAX_VALUE))
                        .queryParam("pickSort", PickSort.LATEST.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Anonymous-Member-Id", anonymousMemberId)
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
        when(amazonS3Client.getUrl(anyString(), anyString())).thenReturn(
                new URL("http", "localhost", 8080, "/xxx.png"));

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
        PutObjectRequest putObjectRequest = createPutObjectRequest(bucket, key, upper10MbMockMultipartFile,
                objectMetadata);

        // when
        PutObjectResult putObjectResult = mock(PutObjectResult.class);

        when(amazonS3Client.putObject(eq(putObjectRequest))).thenReturn(putObjectResult);
        when(amazonS3Client.getUrl(anyString(), anyString())).thenReturn(
                new URL("http", "localhost", 8080, "/xxx.png"));

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
    @DisplayName("익명 회원은 픽픽픽 옵션에 대한 이미지를 삭제할 수 없다.")
    void deleteImageAnonymousException() throws Exception {
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
        PickOptionImage secondPickOptionImage = createPickOptionImage(SECOND_PICK_OPTION_IMAGE, secondImageUrl,
                imageKey);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        RegisterPickOptionRequest firstRegisterPickOptionRequest = createPickOptionRequest("픽옵션1", "픽옵션1블라블라",
                List.of(firstPickOptionImage.getId()));
        RegisterPickOptionRequest secondRegisterPickOptionRequest = createPickOptionRequest("픽옵션2", "픽옵션2블라블라",
                List.of(secondPickOptionImage.getId()));

        Map<PickOptionType, RegisterPickOptionRequest> pickOptions = new HashMap<>();
        pickOptions.put(firstPickOption, firstRegisterPickOptionRequest);
        pickOptions.put(secondPickOption, secondRegisterPickOptionRequest);

        RegisterPickRequest registerPickRequest = createPickRegisterRequest("나의 픽픽픽", pickOptions);

        // when
        // 모킹된 빈에 필요한 의존성을 설정
        ReflectionTestUtils.setField(embeddingRequestHandler, "openAIApiKey", openAIApiKey);
        ReflectionTestUtils.setField(embeddingRequestHandler, "restTemplate", restTemplate);

        // 테스트 할 때마다 open api 를 호출할 수 없기 때문에 mocking 함
        Embedding embedding = new Embedding("mockEmbedding", 1, List.of(1.0));
        OpenAIResponse<Embedding> embeddingOpenAIResponse = new OpenAIResponse<>("mockObject", List.of(embedding),
                "mockModel", new Usage(1, 1));
        when(embeddingRequestHandler.postEmbeddings(any(EmbeddingRequest.class))).thenReturn(
                embeddingOpenAIResponse);

        // then
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
    @DisplayName("익명 회원은 픽픽픽 작성을 할 수 없다.")
    void registerPicksException() throws Exception {
        // given
        String firstImageUrl = "http://devdevdev.co.kr/pickpickpick/fist.jpg";
        String secondImageUrl = "http://devdevdev.co.kr/pickpickpick/second.jpg";
        String imageKey = "/pickpickpick/xxx.jpg";

        PickOptionImage firstPickOptionImage = createPickOptionImage(FIRST_PICK_OPTION_IMAGE, firstImageUrl, imageKey);
        PickOptionImage secondPickOptionImage = createPickOptionImage(SECOND_PICK_OPTION_IMAGE, secondImageUrl,
                imageKey);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        RegisterPickOptionRequest firstRegisterPickOptionRequest = createPickOptionRequest("픽옵션1", "픽옵션1블라블라",
                List.of(firstPickOptionImage.getId()));
        RegisterPickOptionRequest secondRegisterPickOptionRequest = createPickOptionRequest("픽옵션2", "픽옵션2블라블라",
                List.of(secondPickOptionImage.getId()));

        Map<PickOptionType, RegisterPickOptionRequest> pickOptions = new HashMap<>();
        pickOptions.put(firstPickOption, firstRegisterPickOptionRequest);
        pickOptions.put(secondPickOption, secondRegisterPickOptionRequest);

        RegisterPickRequest registerPickRequest = createPickRegisterRequest("나의 픽픽픽", pickOptions);

        // when
        // 모킹된 빈에 필요한 의존성을 설정
        ReflectionTestUtils.setField(embeddingRequestHandler, "openAIApiKey", openAIApiKey);
        ReflectionTestUtils.setField(embeddingRequestHandler, "restTemplate", restTemplate);

        // 테스트 할 때마다 open api 를 호출할 수 없기 때문에 mocking 함
        Embedding embedding = new Embedding("mockEmbedding", 1, List.of(1.0));
        OpenAIResponse<Embedding> embeddingOpenAIResponse = new OpenAIResponse<>("mockObject", List.of(embedding),
                "mockModel", new Usage(1, 1));
        when(embeddingRequestHandler.postEmbeddings(any(EmbeddingRequest.class))).thenReturn(
                embeddingOpenAIResponse);

        // then
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

        PickOption pickOption1 = createPickOption(pick, new Title("픽옵션1제목"), new PickOptionContents("픽옵션1콘텐츠"),
                firstPickOption);
        PickOption pickOption2 = createPickOption(pick, new Title("픽옵션2제목"), new PickOptionContents("픽옵션2콘텐츠"),
                secondPickOption);
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

        Map<PickOptionType, ModifyPickOptionRequest> modifyPickOptionRequests = new HashMap<>();
        modifyPickOptionRequests.put(firstPickOption, modifyPickOptionRequest1);
        modifyPickOptionRequests.put(secondPickOption, modifyPickOptionRequest2);

        ModifyPickRequest modifyPickRequest = createModifyPickRequest("픽타이틀수정", modifyPickOptionRequests);

        // when
        // 모킹된 빈에 필요한 의존성을 설정
        ReflectionTestUtils.setField(embeddingRequestHandler, "openAIApiKey", openAIApiKey);
        ReflectionTestUtils.setField(embeddingRequestHandler, "restTemplate", restTemplate);

        // 테스트 할 때마다 open api 를 호출할 수 없기 때문에 mocking 함
        Embedding embedding = new Embedding("mockEmbedding", 1, List.of(1.0));
        OpenAIResponse<Embedding> embeddingOpenAIResponse = new OpenAIResponse<>("mockObject", List.of(embedding),
                "mockModel", new Usage(1, 1));
        when(embeddingRequestHandler.postEmbeddings(any(EmbeddingRequest.class))).thenReturn(
                embeddingOpenAIResponse);

        // then
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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    @DisplayName("픽픽픽을 수정할 때 픽픽픽 제목이 빈값이거나 null이면 예외가 발생한다.")
    void modifyPickTitleBindException(String pickTitle) throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // == 픽픽픽 작성 환경 == //
        Pick pick = createPick(new Title("픽제목"), member);
        pickRepository.save(pick);

        PickOption pickOption1 = createPickOption(pick, new Title("픽옵션1제목"), new PickOptionContents("픽옵션1콘텐츠"),
                firstPickOption);
        PickOption pickOption2 = createPickOption(pick, new Title("픽옵션2제목"), new PickOptionContents("픽옵션2콘텐츠"),
                secondPickOption);
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

        Map<PickOptionType, ModifyPickOptionRequest> modifyPickOptionRequests = new HashMap<>();
        modifyPickOptionRequests.put(firstPickOption, modifyPickOptionRequest1);
        modifyPickOptionRequests.put(secondPickOption, modifyPickOptionRequest2);

        ModifyPickRequest modifyPickRequest = createModifyPickRequest(pickTitle, modifyPickOptionRequests);

        // when // then
        mockMvc.perform(patch("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(modifyPickRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    @DisplayName("픽픽픽을 수정할 때 픽픽픽 옵션 선택지 제목이 빈값이거나 null이면 예외가 발생한다.")
    void modifyPickPickOptionTitleBindException(String pickOptionTitle) throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // == 픽픽픽 작성 환경 == //
        Pick pick = createPick(new Title("픽제목"), member);
        pickRepository.save(pick);

        PickOption pickOption1 = createPickOption(pick, new Title("픽옵션1제목"), new PickOptionContents("픽옵션1콘텐츠"),
                firstPickOption);
        PickOption pickOption2 = createPickOption(pick, new Title("픽옵션2제목"), new PickOptionContents("픽옵션2콘텐츠"),
                secondPickOption);
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

        ModifyPickOptionRequest modifyPickOptionRequest1 = new ModifyPickOptionRequest(pickOption1.getId(),
                pickOptionTitle,
                "픽옵션1콘텐츠수정", List.of(newPickOption1Image1.getId()));
        ModifyPickOptionRequest modifyPickOptionRequest2 = new ModifyPickOptionRequest(pickOption2.getId(),
                pickOptionTitle,
                "픽옵션2콘텐츠수정", List.of(newPickOption2Image1.getId()));

        Map<PickOptionType, ModifyPickOptionRequest> modifyPickOptionRequests = new HashMap<>();
        modifyPickOptionRequests.put(firstPickOption, modifyPickOptionRequest1);
        modifyPickOptionRequests.put(secondPickOption, modifyPickOptionRequest2);

        ModifyPickRequest modifyPickRequest = createModifyPickRequest("픽타이틀수정", modifyPickOptionRequests);

        // when // then
        mockMvc.perform(patch("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(modifyPickRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Disabled
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    @DisplayName("픽픽픽을 수정할 때 픽픽픽 옵션 선택지 내용이 빈값이거나 null이면 예외가 발생한다.")
    void modifyPickPickOptionContentBindException(String pickOptionContent) throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // == 픽픽픽 작성 환경 == //
        Pick pick = createPick(new Title("픽제목"), member);
        pickRepository.save(pick);

        PickOption pickOption1 = createPickOption(pick, new Title("픽옵션1제목"), new PickOptionContents("픽옵션1콘텐츠"),
                firstPickOption);
        PickOption pickOption2 = createPickOption(pick, new Title("픽옵션2제목"), new PickOptionContents("픽옵션2콘텐츠"),
                secondPickOption);
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
                pickOptionContent, List.of(newPickOption1Image1.getId()));
        ModifyPickOptionRequest modifyPickOptionRequest2 = new ModifyPickOptionRequest(pickOption2.getId(), "픽옵션2제목수정",
                pickOptionContent, List.of(newPickOption2Image1.getId()));

        Map<PickOptionType, ModifyPickOptionRequest> modifyPickOptionRequests = new HashMap<>();
        modifyPickOptionRequests.put(firstPickOption, modifyPickOptionRequest1);
        modifyPickOptionRequests.put(secondPickOption, modifyPickOptionRequest2);

        ModifyPickRequest modifyPickRequest = createModifyPickRequest("픽타이틀수정", modifyPickOptionRequests);

        // when // then
        mockMvc.perform(patch("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(modifyPickRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("픽픽픽을 수정할 때 픽픽픽 옵션 선택지 아이디가 null이면 예외가 발생한다.")
    void modifyPickPickOptionIdBindException(Long pickOptionId) throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // == 픽픽픽 작성 환경 == //
        Pick pick = createPick(new Title("픽제목"), member);
        pickRepository.save(pick);

        PickOption pickOption1 = createPickOption(pick, new Title("픽옵션1제목"), new PickOptionContents("픽옵션1콘텐츠"),
                firstPickOption);
        PickOption pickOption2 = createPickOption(pick, new Title("픽옵션2제목"), new PickOptionContents("픽옵션2콘텐츠"),
                secondPickOption);
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

        ModifyPickOptionRequest modifyPickOptionRequest1 = new ModifyPickOptionRequest(pickOptionId, "픽옵션1제목수정",
                "픽옵션1콘텐츠수정", List.of(newPickOption1Image1.getId()));
        ModifyPickOptionRequest modifyPickOptionRequest2 = new ModifyPickOptionRequest(pickOptionId, "픽옵션2제목수정",
                "픽옵션2콘텐츠수정", List.of(newPickOption2Image1.getId()));

        Map<PickOptionType, ModifyPickOptionRequest> modifyPickOptionRequests = new HashMap<>();
        modifyPickOptionRequests.put(firstPickOption, modifyPickOptionRequest1);
        modifyPickOptionRequests.put(secondPickOption, modifyPickOptionRequest2);

        ModifyPickRequest modifyPickRequest = createModifyPickRequest("픽타이틀수정", modifyPickOptionRequests);

        // when // then
        mockMvc.perform(patch("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(modifyPickRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("회원이 자신이 작성한 픽픽픽 상세를 조회한다.")
    void findPickDetail() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 제목"), new Count(1), new Count(0), new Count(0), new Count(0),
                ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, new Title("픽픽픽 옵션1"), new PickOptionContents("픽픽픽 옵션1 내용"),
                new Count(1), PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션2"), new PickOptionContents("픽픽픽 옵션2 내용"),
                new Count(0), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 옵션 이미지 생성
        PickOptionImage firstPickOptionImage = createPickOptionImage("이미지1", "http://iamge1.png", firstPickOption);
        PickOptionImage secondPickOptionImage = createPickOptionImage("이미지2", "http://iamge2.png", secondPickOption);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        // 픽픽픽 옵션 투표 여부
        PickVote pickVote = createPickVote(member, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.userId").isString())
                .andExpect(jsonPath("$.data.nickname").isString())
                .andExpect(jsonPath("$.data.pickCreatedAt").isString())
                .andExpect(jsonPath("$.data.pickTitle").isString())
                .andExpect(jsonPath("$.data.isAuthor").isBoolean())
                .andExpect(jsonPath("$.data.isVoted").isBoolean())

                .andExpect(jsonPath("$.data.pickOptions").isNotEmpty())

                .andExpect(jsonPath("$.data.pickOptions.firstPickOption").isNotEmpty())
                .andExpect(jsonPath("$.data.pickOptions.firstPickOption.id").isNumber())
                .andExpect(jsonPath("$.data.pickOptions.firstPickOption.title").isString())
                .andExpect(jsonPath("$.data.pickOptions.firstPickOption.isPicked").isBoolean())
                .andExpect(jsonPath("$.data.pickOptions.firstPickOption.percent").isNumber())
                .andExpect(jsonPath("$.data.pickOptions.firstPickOption.content").isString())
                .andExpect(jsonPath("$.data.pickOptions.firstPickOption.voteTotalCount").isNumber())
                .andExpect(jsonPath("$.data.pickOptions.firstPickOption.pickDetailOptionImages").isArray())
                .andExpect(jsonPath("$.data.pickOptions.firstPickOption.pickDetailOptionImages[0].id").isNumber())
                .andExpect(jsonPath("$.data.pickOptions.firstPickOption.pickDetailOptionImages[0].imageUrl").isString())

                .andExpect(jsonPath("$.data.pickOptions.secondPickOption").isNotEmpty())
                .andExpect(jsonPath("$.data.pickOptions.secondPickOption.id").isNumber())
                .andExpect(jsonPath("$.data.pickOptions.secondPickOption.title").isString())
                .andExpect(jsonPath("$.data.pickOptions.secondPickOption.isPicked").isBoolean())
                .andExpect(jsonPath("$.data.pickOptions.secondPickOption.percent").isNumber())
                .andExpect(jsonPath("$.data.pickOptions.secondPickOption.content").isString())
                .andExpect(jsonPath("$.data.pickOptions.secondPickOption.voteTotalCount").isNumber())
                .andExpect(jsonPath("$.data.pickOptions.secondPickOption.pickDetailOptionImages").isArray())
                .andExpect(jsonPath("$.data.pickOptions.secondPickOption.pickDetailOptionImages[0].id").isNumber())
                .andExpect(
                        jsonPath("$.data.pickOptions.secondPickOption.pickDetailOptionImages[0].imageUrl").isString());
    }

    @Test
    @DisplayName("픽픽픽 옵션에 투표한 이력이 없는 회원이 픽픽픽 옵션 중 하나에 투표한다.")
    void votePickOption() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 제목"), new Count(0), new Count(0), new Count(0), new Count(0),
                ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(new Title("첫번째 픽옵션 제목"), new Count(0),
                PickOptionType.firstPickOption, pick);
        PickOption secondPickOption = createPickOption(new Title("두번째 픽옵션 제목"), new Count(0),
                PickOptionType.secondPickOption, pick);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        VotePickOptionRequest request = VotePickOptionRequest.builder()
                .pickId(pick.getId())
                .pickOptionId(firstPickOption.getId())
                .build();

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/picks/vote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.pickId").isNumber())
                .andExpect(jsonPath("$.data.votePickOptions").isArray())
                .andExpect(jsonPath("$.data.votePickOptions.[0].pickOptionId").isNumber())
                .andExpect(jsonPath("$.data.votePickOptions.[0].pickVoteId").isNumber())
                .andExpect(jsonPath("$.data.votePickOptions.[0].voteTotalCount").isNumber())
                .andExpect(jsonPath("$.data.votePickOptions.[0].percent").isNumber())
                .andExpect(jsonPath("$.data.votePickOptions.[0].isPicked").isBoolean())
                .andExpect(jsonPath("$.data.votePickOptions.[1].pickOptionId").isNumber())
                .andExpect(jsonPath("$.data.votePickOptions.[1].pickVoteId").isEmpty())
                .andExpect(jsonPath("$.data.votePickOptions.[1].voteTotalCount").isNumber())
                .andExpect(jsonPath("$.data.votePickOptions.[1].percent").isNumber())
                .andExpect(jsonPath("$.data.votePickOptions.[1].isPicked").isBoolean());
    }

    @Test
    @DisplayName("픽픽픽 선택지에 투표할 때 pickId와 pcikOptionId가 null 이면 예외가 발생한다.")
    void votePickOptionBindException() throws Exception {
        // given
        VotePickOptionRequest request = VotePickOptionRequest.builder()
                .pickId(null)
                .pickOptionId(null)
                .build();

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/picks/vote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("회원이 픽픽픽 옵션을 투표할 때 픽픽픽이 없으면 예외가 발생한다.")
    void votePickOption_INVALID_ANONYMOUS_MEMBER_ID_MESSAGE() throws Exception {
        // given
        VotePickOptionRequest request = VotePickOptionRequest.builder()
                .pickId(0L)
                .pickOptionId(0L)
                .build();

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/picks/vote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @DisplayName("회원이 자신이 작성한 픽픽픽을 삭제한다.")
    void deletePick() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), member);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption fistPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(fistPickOption, secondPickOption));

        // 픽픽픽 이미지 생성
        PickOptionImage firstPickOptionImage = createPickOptionImage("firstPickOptionImage", fistPickOption);
        PickOptionImage secondPickOptionImage = createPickOptionImage("secondPickOptionImage", fistPickOption);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(member, fistPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        // when // then
        mockMvc.perform(delete("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()));
    }

    @Test
    @DisplayName("회원 자신이 작성한 픽픽픽이 아니면 삭제할 수 없다.")
    void deletePickNotAuthor() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                "흑빛파티시엘", password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), author);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption fistPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(fistPickOption, secondPickOption));

        // 픽픽픽 이미지 생성
        PickOptionImage firstPickOptionImage = createPickOptionImage("firstPickOptionImage", fistPickOption);
        PickOptionImage secondPickOptionImage = createPickOptionImage("secondPickOptionImage", fistPickOption);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(author, fistPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        // when // then
        mockMvc.perform(delete("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @DisplayName("나도 고민했는데 픽픽픽을 조회한다.")
    void getSimilarPicks() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        Pick targetPick = createPick(new Title("유소영"), new Count(1), new Count(1), member, ContentStatus.APPROVAL,
                List.of(1.0, 1.0, 1.0));
        Pick pick1 = createPick(new Title("유쏘영"), new Count(2), new Count(5), member, ContentStatus.APPROVAL,
                List.of(0.1, 0.2, 0.3));
        Pick pick2 = createPick(new Title("소영쏘"), new Count(3), new Count(4), member, ContentStatus.APPROVAL,
                List.of(0.2, 0.3, 0.4));
        Pick pick3 = createPick(new Title("쏘영쏘"), new Count(4), new Count(3), member, ContentStatus.APPROVAL,
                List.of(0.3, 0.4, 0.5));
        Pick pick4 = createPick(new Title("쏘주쏘"), new Count(5), new Count(2), member, ContentStatus.READY,
                List.of(0.4, 0.5, 0.6));
        Pick pick5 = createPick(new Title("쏘주"), new Count(6), new Count(1), member, ContentStatus.REJECT,
                List.of(0.4, 0.5, 0.6));
        pickRepository.saveAll(List.of(targetPick, pick1, pick2, pick3, pick4, pick5));

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}/similarties", targetPick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.datas").isArray())
                .andExpect(jsonPath("$.datas.[0].id").isNumber())
                .andExpect(jsonPath("$.datas.[0].title").isString())
                .andExpect(jsonPath("$.datas.[0].voteTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[0].commentTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[0].similarity").isNumber())
                .andExpect(jsonPath("$.datas.[1].id").isNumber())
                .andExpect(jsonPath("$.datas.[1].title").isString())
                .andExpect(jsonPath("$.datas.[1].voteTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[1].commentTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[1].similarity").isNumber())
                .andExpect(jsonPath("$.datas.[2].id").isNumber())
                .andExpect(jsonPath("$.datas.[2].title").isString())
                .andExpect(jsonPath("$.datas.[2].voteTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[2].commentTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[2].similarity").isNumber());
    }

    @Test
    @DisplayName("나도 고민했는데 픽픽픽을 조회할 때 타겟 픽픽픽이 존재하지 않으면 예외가 발생한다.")
    void getSimilarPicks_NOT_FOUND() throws Exception {
        // given // when // then
        mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}/similarties", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @DisplayName("나도 고민했는데 픽픽픽을 조회할 때 타겟 픽픽픽이 승인상태가 아니면 예외가 발생한다.")
    void getSimilarPicks_BAD_REQUEST() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 거절상태의 타겟 픽픽픽
        Pick targetPick = createPick(new Title("유소영"), new Count(1), new Count(1), member, ContentStatus.REJECT,
                List.of(1.0, 1.0, 1.0));

        Pick pick1 = createPick(new Title("유쏘영"), new Count(2), new Count(5), member, ContentStatus.APPROVAL,
                List.of(0.1, 0.2, 0.3));
        Pick pick2 = createPick(new Title("소영쏘"), new Count(3), new Count(4), member, ContentStatus.APPROVAL,
                List.of(0.2, 0.3, 0.4));
        Pick pick3 = createPick(new Title("쏘영쏘"), new Count(4), new Count(3), member, ContentStatus.APPROVAL,
                List.of(0.3, 0.4, 0.5));
        Pick pick4 = createPick(new Title("쏘주쏘"), new Count(5), new Count(2), member, ContentStatus.READY,
                List.of(0.4, 0.5, 0.6));
        Pick pick5 = createPick(new Title("쏘주"), new Count(6), new Count(1), member, ContentStatus.REJECT,
                List.of(0.4, 0.5, 0.6));
        pickRepository.saveAll(List.of(targetPick, pick1, pick2, pick3, pick4, pick5));

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}/similarties", targetPick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    private Pick createPick(Title title, ContentStatus contentStatus, Member member) {
        return Pick.builder()
                .title(title)
                .contentStatus(contentStatus)
                .member(member)
                .build();
    }

    private Pick createPick(Title title, Count pickVoteCount, Count commentTotalCount, Member member,
                            ContentStatus contentStatus, List<Double> embeddings) {
        return Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteCount)
                .commentTotalCount(commentTotalCount)
                .member(member)
                .contentStatus(contentStatus)
                .embeddings(embeddings)
                .build();
    }

    private PickOption createPickOption(Title title, Count voteTotalCount, PickOptionType pickOptionType, Pick pick) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .voteTotalCount(voteTotalCount)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private Pick createPick(Title title, Count viewTotalCount, Count commentTotalCount, Count voteTotalCount,
                            Count poplarScore, ContentStatus contentStatus, Member member) {
        return Pick.builder()
                .title(title)
                .viewTotalCount(viewTotalCount)
                .voteTotalCount(voteTotalCount)
                .commentTotalCount(commentTotalCount)
                .popularScore(poplarScore)
                .contentStatus(contentStatus)
                .member(member)
                .build();
    }

    private PickOptionImage createPickOptionImage(String name, String imageUrl, PickOption pickOption) {
        PickOptionImage pickOptionImage = PickOptionImage.builder()
                .name(name)
                .imageUrl(imageUrl)
                .imageKey("imageKey")
                .build();

        pickOptionImage.changePickOption(pickOption);

        return pickOptionImage;
    }

    private PickVote createPickVote(Member member, PickOption pickOption, Pick pick) {
        PickVote pickVote = PickVote.builder()
                .member(member)
                .pickOption(pickOption)
                .pick(pick)
                .build();

        pickVote.changePick(pick);

        return pickVote;
    }

    private Pick createPick(Title title, Count pickVoteCount, Member member) {
        return Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteCount)
                .member(member)
                .build();
    }

    private PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents,
                                        Count pickOptionVoteCount,
                                        PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(pickOptionVoteCount)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
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
                .imageKey("imageKey")
                .imageUrl("imageUrl")
                .build();
    }

    private PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents,
                                        PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private PickOptionImage createPickOptionImage(String name, PickOption pickOption) {
        PickOptionImage pickOptionImage = PickOptionImage.builder()
                .name(name)
                .imageKey("imageKey")
                .imageUrl("imageUrl")
                .build();

        pickOptionImage.changePickOption(pickOption);

        return pickOptionImage;
    }


    private ModifyPickRequest createModifyPickRequest(String pickTitle,
                                                      Map<PickOptionType, ModifyPickOptionRequest> modifyPickOptionRequests) {
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

    private RegisterPickRequest createPickRegisterRequest(String pickTitle,
                                                          Map<PickOptionType, RegisterPickOptionRequest> pickOptions) {
        return RegisterPickRequest.builder()
                .pickTitle(pickTitle)
                .pickOptions(pickOptions)
                .build();
    }

    private RegisterPickOptionRequest createPickOptionRequest(String pickOptionTitle, String pickOptionContent,
                                                              List<Long> pickOptionImageIds) {
        return RegisterPickOptionRequest.builder()
                .pickOptionTitle(pickOptionTitle)
                .pickOptionContent(pickOptionContent)
                .pickOptionImageIds(pickOptionImageIds)
                .build();
    }

    private PickOptionImage createPickOptionImage(String imageUrl, String imageKey) {
        return PickOptionImage.builder()
                .name("imageName")
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

    private SocialMemberDto createSocialDto(String userId, String name, String nickName, String password, String email,
                                            String socialType, String role) {
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

    private Pick createPick(Member member, Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                            Count pickcommentTotalCount, String thumbnailUrl, String author,
                            ContentStatus contentStatus,
                            List<PickOption> pickOptions, List<PickVote> pickVotes
    ) {

        Pick pick = Pick.builder()
                .member(member)
                .title(title)
                .voteTotalCount(pickVoteTotalCount)
                .viewTotalCount(pickViewTotalCount)
                .commentTotalCount(pickcommentTotalCount)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .contentStatus(contentStatus)
                .build();

        pick.changePickOptions(pickOptions);
        pick.changePickVote(pickVotes);

        return pick;
    }

    private PickOption createPickOption(Title title, PickOptionContents pickOptionContents, Count voteTotalCount,
                                        PickOptionType pickOptionType) {
        return PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(voteTotalCount)
                .pickOptionType(pickOptionType)
                .build();
    }
}