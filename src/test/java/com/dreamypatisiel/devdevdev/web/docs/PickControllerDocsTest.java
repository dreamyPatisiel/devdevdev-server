package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType.firstPickOption;
import static com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType.secondPickOption;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickService.FIRST_PICK_OPTION_IMAGE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickService.SECOND_PICK_OPTION_IMAGE;
import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.authenticationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.numberOrNull;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.pickOptionImageNameType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.pickSortType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.yearMonthDateTimeType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NULL;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
import com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickService;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.openai.request.EmbeddingRequest;
import com.dreamypatisiel.devdevdev.openai.response.Embedding;
import com.dreamypatisiel.devdevdev.openai.response.OpenAIResponse;
import com.dreamypatisiel.devdevdev.openai.response.Usage;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.VotePickOptionRequest;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

public class PickControllerDocsTest extends SupportControllerDocsTest {

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
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        Title title = new Title("픽1타이틀");
        Count count = new Count(2);

        Pick pick = createPick(title, count, count, count, count, ContentStatus.APPROVAL, member);
        pick.changePopularScore(pickPopularScorePolicy);
        pickRepository.save(pick);

        PickOption pickOption1 = createPickOption(pick, new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"),
                new Count(1), firstPickOption);
        PickOption pickOption2 = createPickOption(pick, new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"),
                new Count(1), secondPickOption);
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/picks")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("pickId", String.valueOf(Long.MAX_VALUE))
                        .queryParam("pickSort", PickSort.LATEST.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("pick-main",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰"),
                        headerWithName("Anonymous-Member-Id").optional().description("익명 회원 아이디")
                ),
                queryParameters(
                        parameterWithName("pickId").optional().description("픽픽픽 아이디"),
                        parameterWithName("pickSort").optional().description("픽픽픽 정렬 조건").attributes(pickSortType()),
                        parameterWithName("size").optional().description("조회되는 데이터 수")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),

                        fieldWithPath("data.content").type(ARRAY).description("픽픽픽 메인 배열"),
                        fieldWithPath("data.content[].id").type(NUMBER).description("픽픽픽 아이디"),
                        fieldWithPath("data.content[].title").type(STRING).description("픽픽픽 제목"),
                        fieldWithPath("data.content[].voteTotalCount").type(NUMBER).description("픽픽픽 전체 투표 수"),
                        fieldWithPath("data.content[].commentTotalCount").type(NUMBER).description("픽픽픽 전체 댓글 수"),
                        fieldWithPath("data.content[].viewTotalCount").type(NUMBER).description("픽픽픽 조회 수"),
                        fieldWithPath("data.content[].popularScore").type(NUMBER).description("픽픽픽 인기점수"),
                        fieldWithPath("data.content[].isVoted").attributes(authenticationType()).type(BOOLEAN)
                                .description("픽픽픽 투표 여부(익명 사용자는 필드가 없다.)"),

                        fieldWithPath("data.content[].pickOptions").type(ARRAY).description("픽픽픽 옵션 배열"),
                        fieldWithPath("data.content[].pickOptions[].id").type(NUMBER).description("픽픽픽 옵션 아이디"),
                        fieldWithPath("data.content[].pickOptions[].title").type(STRING).description("픽픽픽 옵션 제목"),
                        fieldWithPath("data.content[].pickOptions[].percent").type(NUMBER).description("픽픽픽 옵션 투표율(%)"),
                        fieldWithPath("data.content[].pickOptions[].isPicked").attributes(authenticationType()).type(
                                BOOLEAN).description("픽픽픽 옵션 투표 여부(익명 사용자는 필드가 없다.)"),
                        fieldWithPath("data.content[].pickOptions[].id").type(NUMBER).description("픽픽픽 옵션 아이디"),
                        fieldWithPath("data.content[].pickOptions[].title").type(STRING).description("픽픽픽 옵션 제목"),
                        fieldWithPath("data.content[].pickOptions[].percent").type(NUMBER).description("픽픽픽 옵션 투표율(%)"),
                        fieldWithPath("data.content[].pickOptions[].isPicked").attributes(authenticationType()).type(
                                BOOLEAN).description("픽픽픽 옵션 투표 여부(익명 사용자는 필드가 없다.)"),

                        fieldWithPath("data.pageable").type(OBJECT).description("픽픽픽 메인 페이지네이션 정보"),
                        fieldWithPath("data.pageable.pageNumber").type(NUMBER).description("페이지 번호"),
                        fieldWithPath("data.pageable.pageSize").type(NUMBER).description("페이지 사이즈"),

                        fieldWithPath("data.pageable.sort").type(OBJECT).description("정렬 정보"),
                        fieldWithPath("data.pageable.sort.empty").type(BOOLEAN).description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.pageable.sort.sorted").type(BOOLEAN).description("정렬 여부"),
                        fieldWithPath("data.pageable.sort.unsorted").type(BOOLEAN).description("비정렬 여부"),

                        fieldWithPath("data.pageable.offset").type(NUMBER).description("페이지 오프셋 (페이지 크기 * 페이지 번호)"),
                        fieldWithPath("data.pageable.paged").type(BOOLEAN).description("페이지 정보 포함 여부"),
                        fieldWithPath("data.pageable.unpaged").type(BOOLEAN).description("페이지 정보 비포함 여부"),

                        fieldWithPath("data.first").type(BOOLEAN).description("현재 페이지가 첫 페이지 여부"),
                        fieldWithPath("data.last").type(BOOLEAN).description("현재 페이지가 마지막 페이지 여부"),
                        fieldWithPath("data.size").type(NUMBER).description("페이지 크기"),
                        fieldWithPath("data.number").type(NUMBER).description("현재 페이지"),

                        fieldWithPath("data.sort").type(OBJECT).description("정렬 정보"),
                        fieldWithPath("data.sort.empty").type(BOOLEAN).description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.sort.sorted").type(BOOLEAN).description("정렬 상태 여부"),
                        fieldWithPath("data.sort.unsorted").type(BOOLEAN).description("비정렬 상태 여부"),
                        fieldWithPath("data.numberOfElements").type(NUMBER).description("현재 페이지 데이터 수"),
                        fieldWithPath("data.empty").type(BOOLEAN).description("현재 빈 페이지 여부")
                )
        ));
    }

    @Test
    @DisplayName("픽픽픽 이미지를 업로드 한다.")
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
        ResultActions actions = mockMvc.perform(multipart(HttpMethod.POST, "/devdevdev/api/v1/picks/image")
                        .file(mockMultipartFile)
                        .queryParam("name", MemberPickService.FIRST_PICK_OPTION_IMAGE)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("pick-main-option-image",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                queryParameters(
                        parameterWithName("name").description("픽픽픽 옵션 이미지 이름").attributes(pickOptionImageNameType())
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과")
                                .attributes(authenticationType()),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pickOptionImages").type(ARRAY).description("픽픽픽 옵션 이미지 배열")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pickOptionImages[].name").type(STRING).description("픽픽픽 옵션 이미지 이름")
                                .attributes(pickOptionImageNameType(), authenticationType()),
                        fieldWithPath("data.pickOptionImages[].pickOptionImageId").type(NUMBER)
                                .description("픽픽픽 옵션 이미지 아이디")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pickOptionImages[].imageUrl").type(STRING).description("픽픽픽 옵션 이미지 URL")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pickOptionImages[].imageKey").type(STRING).description("픽픽픽 옵션 이미지 KEY(경로)")
                                .attributes(authenticationType())
                )
        ));
    }

    @Test
    @DisplayName("익명 회원은 픽픽픽 이미지를 업로드에 실패한다.")
    void uploadPickOptionImagesException() throws Exception {
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
        ResultActions actions = mockMvc.perform(multipart(HttpMethod.POST, "/devdevdev/api/v1/picks/image")
                        .file(mockMultipartFile)
                        .queryParam("name", MemberPickService.FIRST_PICK_OPTION_IMAGE)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("pick-main-option-image-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                queryParameters(
                        parameterWithName("name").description("픽픽픽 옵션 이미지 이름").attributes(pickOptionImageNameType())
                ),
                exceptionResponseFields()
        ));
    }

    @Test
    @DisplayName("이미지 형식에 맞지 않으면 픽픽픽 이미지를 업로드에 실패한다.")
    void uploadPickOptionImagesMediaTypeException() throws Exception {
        // given
        MockMultipartFile mockMultipartFile = createMockMultipartFile("pickOptionImages",
                "tesImage.gif", MediaType.IMAGE_GIF_VALUE);
        String bucket = "bucket";
        String key = "/pick/pickOption/image/xxx.gif";

        ObjectMetadata objectMetadata = createObjectMetadataByMultipartFile(mockMultipartFile);
        PutObjectRequest putObjectRequest = createPutObjectRequest(bucket, key, mockMultipartFile, objectMetadata);

        // when
        PutObjectResult putObjectResult = mock(PutObjectResult.class);

        when(amazonS3Client.putObject(eq(putObjectRequest))).thenReturn(putObjectResult);
        when(amazonS3Client.getUrl(anyString(), anyString())).thenReturn(
                new URL("http", "localhost", 8080, "/xxx.gif"));

        // then
        ResultActions actions = mockMvc.perform(multipart(HttpMethod.POST, "/devdevdev/api/v1/picks/image")
                        .file(mockMultipartFile)
                        .queryParam("name", MemberPickService.FIRST_PICK_OPTION_IMAGE)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("pick-main-option-image-media-type-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                queryParameters(
                        parameterWithName("name").description("픽픽픽 옵션 이미지 이름").attributes(pickOptionImageNameType())
                ),
                exceptionResponseFields()
        ));
    }

    @Test
    @DisplayName("이미지를 업로드할 때 3개 초과로 업로드하면 예외가 발생한다.")
    void uploadPickOptionImageSizeException() throws Exception {
        // given
        MockMultipartFile mockMultipartFile1 = createMockMultipartFile("pickOptionImages", "tesImage1.png");
        MockMultipartFile mockMultipartFile2 = createMockMultipartFile("pickOptionImages", "tesImage2.png");
        MockMultipartFile mockMultipartFile3 = createMockMultipartFile("pickOptionImages", "tesImage3.png");
        MockMultipartFile mockMultipartFile4 = createMockMultipartFile("pickOptionImages", "tesImage4.png");

        // when // then
        ResultActions actions = mockMvc.perform(multipart(HttpMethod.POST, "/devdevdev/api/v1/picks/image")
                        .file(mockMultipartFile1)
                        .file(mockMultipartFile2)
                        .file(mockMultipartFile3)
                        .file(mockMultipartFile4)
                        .queryParam("name", MemberPickService.FIRST_PICK_OPTION_IMAGE)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("pick-main-option-image-size-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                queryParameters(
                        parameterWithName("name").description("픽픽픽 옵션 이미지 이름").attributes(pickOptionImageNameType())
                ),
                exceptionResponseFields()
        ));
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
        ResultActions actions = mockMvc.perform(delete("/devdevdev/api/v1/picks/image/{pickOptionImageId}"
                        , pickOptionImage.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("pick-main-option-delete-image",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickOptionImageId").description("픽픽픽 옵션 이미지 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과")
                                .attributes(authenticationType())
                )
        ));
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
        ResultActions actions = mockMvc.perform(
                        delete("/devdevdev/api/v1/picks/image/{pickOptionImageId}"
                                , pickOptionImage.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isForbidden());

        // docs
        actions.andDo(document("pick-main-option-delete-image-anonymous-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("pickOptionImageId").description("픽픽픽 옵션 이미지 아이디")
                ),
                exceptionResponseFields()
        ));
    }

    @Test
    @DisplayName("회원은 존재하지 않은 픽픽픽 옵션에 대한 이미지를 삭제할 수 없다.")
    void deletePickImageFileException() throws Exception {
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

        // when
        PutObjectResult putObjectResult = mock(PutObjectResult.class);

        when(amazonS3Client.putObject(eq(putObjectRequest))).thenReturn(putObjectResult);
        when(amazonS3Client.getUrl(anyString(), anyString())).thenReturn(url);

        // then
        ResultActions actions = mockMvc.perform(
                        delete("/devdevdev/api/v1/picks/image/{pickOptionImageId}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("pick-main-option-delete-image-not-found-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickOptionImageId").description("픽픽픽 옵션 이미지 아이디")
                ),
                exceptionResponseFields()
        ));
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

        RegisterPickOptionRequest firstRegisterPickOptionRequest = createPickOptionRequest("Svelte가 짱이다!",
                "낮은 러닝커브 그리고 빠른 속도!",
                List.of(firstPickOptionImage.getId()));
        RegisterPickOptionRequest secondRegisterPickOptionRequest = createPickOptionRequest("React가 짱이다!",
                "대형 커뮤니티, 대기업에서 라이브러리 관리!",
                List.of(secondPickOptionImage.getId()));

        Map<PickOptionType, RegisterPickOptionRequest> pickOptions = new HashMap<>();
        pickOptions.put(firstPickOption, firstRegisterPickOptionRequest);
        pickOptions.put(secondPickOption, secondRegisterPickOptionRequest);

        RegisterPickRequest registerPickRequest = createPickRegisterRequest("Svelte VS React", pickOptions);

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
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/picks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(registerPickRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("pick-register",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                requestFields(
                        fieldWithPath("pickTitle").type(STRING).description("픽픽픽 타이틀"),
                        fieldWithPath("pickOptions").type(OBJECT).description("픽픽픽 옵션"),
                        fieldWithPath("pickOptions.firstPickOption").type(OBJECT).description("픽픽픽 첫 번째 옵션 선택지"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionTitle").type(STRING)
                                .description("픽픽픽 첫 번째 옵션 선택지 제목"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionContent").type(STRING)
                                .description("픽픽픽 첫 번째 옵션 선택지 내용"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionImageIds").type(ARRAY)
                                .description("픽픽픽 첫 번째 옵션 이미지 아이디 배열"),
                        fieldWithPath("pickOptions.secondPickOption").type(OBJECT).description("픽픽픽 두 번째 옵션 선택지"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionTitle").type(STRING)
                                .description("픽픽픽 두 번째 옵션 선택지 제목"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionContent").type(STRING)
                                .description("픽픽픽 두 번째 옵션 선택지 내용"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionImageIds").type(ARRAY)
                                .description("픽픽픽 두 번째 옵션 이미지 아이디 배열")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과")
                                .attributes(authenticationType()),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pickId").type(NUMBER).description("픽픽픽 아이디")
                                .attributes(authenticationType())
                )
        ));
    }

    @Test
    @DisplayName("익명 회원은 픽픽픽 작성을 할 수 없다.")
    void registerPicksAnonymousException() throws Exception {
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
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/picks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(registerPickRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());

        // docs
        actions.andDo(document("pick-register-anonymous-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                        fieldWithPath("pickTitle").type(STRING).description("픽픽픽 타이틀"),
                        fieldWithPath("pickOptions").type(OBJECT).description("픽픽픽 옵션"),
                        fieldWithPath("pickOptions.firstPickOption").type(OBJECT).description("픽픽픽 첫 번째 옵션 선택지"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionTitle").type(STRING)
                                .description("픽픽픽 첫 번째 옵션 선택지 제목"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionContent").type(STRING)
                                .description("픽픽픽 첫 번째 옵션 선택지 내용"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionImageIds").type(ARRAY)
                                .description("픽픽픽 첫 번째 옵션 이미지 아이디 배열"),
                        fieldWithPath("pickOptions.secondPickOption").type(OBJECT).description("픽픽픽 두 번째 옵션 선택지"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionTitle").type(STRING)
                                .description("픽픽픽 두 번째 옵션 선택지 제목"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionContent").type(STRING)
                                .description("픽픽픽 두 번째 옵션 선택지 내용"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionImageIds").type(ARRAY)
                                .description("픽픽픽 두 번째 옵션 이미지 아이디 배열")
                ),
                exceptionResponseFields()
        ));
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
        ResultActions actions = mockMvc.perform(patch("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(modifyPickRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("pick-modify",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디")
                ),
                requestFields(
                        fieldWithPath("pickTitle").type(STRING).description("픽픽픽 타이틀"),
                        fieldWithPath("pickOptions").type(OBJECT).description("픽픽픽 옵션"),
                        fieldWithPath("pickOptions.firstPickOption").type(OBJECT).description("픽픽픽 첫 번째 옵션 선택지"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionId").type(NUMBER)
                                .description("픽픽픽 첫 번째 옵션 선택지 아이디"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionTitle").type(STRING)
                                .description("픽픽픽 첫 번째 옵션 선택지 제목"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionContent").type(STRING)
                                .description("픽픽픽 첫 번째 옵션 선택지 내용"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionImageIds").type(ARRAY)
                                .description("픽픽픽 첫 번째 옵션 이미지 아이디 배열"),
                        fieldWithPath("pickOptions.secondPickOption").type(OBJECT).description("픽픽픽 두 번째 옵션 선택지"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionId").type(NUMBER)
                                .description("픽픽픽 두 번째 옵션 선택지 아이디"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionTitle").type(STRING)
                                .description("픽픽픽 두 번째 옵션 선택지 제목"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionContent").type(STRING)
                                .description("픽픽픽 두 번째 옵션 선택지 내용"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionImageIds").type(ARRAY)
                                .description("픽픽픽 두 번째 옵션 이미지 아이디 배열")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과")
                                .attributes(authenticationType()),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pickId").type(NUMBER).description("픽픽픽 아이디")
                                .attributes(authenticationType())
                )
        ));
    }

    @Test
    @DisplayName("픽픽픽을 수정할 때 회원 본인이 작성하지 않은 픽픽픽이면 예외가 발생한다.")
    void modifyPickAccessDeniedException() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        SocialMemberDto otherSocialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", "ohterDreamy5patisiel@gmail.com", socialType, role);
        Member otherMember = Member.createMemberBy(otherSocialMemberDto);
        memberRepository.save(otherMember);

        // == 픽픽픽 작성 환경 == //
        Pick pick = createPick(new Title("픽제목"), otherMember);
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
        ResultActions actions = mockMvc.perform(patch("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(modifyPickRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());

        // docs
        actions.andDo(document("pick-modify-access-denied-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                requestFields(
                        fieldWithPath("pickTitle").type(STRING).description("픽픽픽 타이틀"),
                        fieldWithPath("pickOptions").type(OBJECT).description("픽픽픽 옵션"),
                        fieldWithPath("pickOptions.firstPickOption").type(OBJECT).description("픽픽픽 첫 번째 옵션 선택지"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionId").type(NUMBER)
                                .description("픽픽픽 첫 번째 옵션 선택지 아이디"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionTitle").type(STRING)
                                .description("픽픽픽 첫 번째 옵션 선택지 제목"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionContent").type(STRING)
                                .description("픽픽픽 첫 번째 옵션 선택지 내용"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionImageIds").type(ARRAY)
                                .description("픽픽픽 첫 번째 옵션 이미지 아이디 배열"),
                        fieldWithPath("pickOptions.secondPickOption").type(OBJECT).description("픽픽픽 두 번째 옵션 선택지"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionId").type(NUMBER)
                                .description("픽픽픽 두 번째 옵션 선택지 아이디"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionTitle").type(STRING)
                                .description("픽픽픽 두 번째 옵션 선택지 제목"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionContent").type(STRING)
                                .description("픽픽픽 두 번째 옵션 선택지 내용"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionImageIds").type(ARRAY)
                                .description("픽픽픽 두 번째 옵션 이미지 아이디 배열")
                ),
                exceptionResponseFields()
        ));
    }

    @ParameterizedTest
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
        ResultActions actions = mockMvc.perform(patch("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(modifyPickRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("pick-modify-pick-title-bind-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                requestFields(
                        fieldWithPath("pickTitle").type(STRING).description("픽픽픽 타이틀"),
                        fieldWithPath("pickOptions").type(OBJECT).description("픽픽픽 옵션"),
                        fieldWithPath("pickOptions.firstPickOption").type(OBJECT).description("픽픽픽 첫 번째 옵션 선택지"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionId").type(NUMBER)
                                .description("픽픽픽 첫 번째 옵션 선택지 아이디"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionTitle").type(STRING)
                                .description("픽픽픽 첫 번째 옵션 선택지 제목"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionContent").type(STRING)
                                .description("픽픽픽 첫 번째 옵션 선택지 내용"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionImageIds").type(ARRAY)
                                .description("픽픽픽 첫 번째 옵션 이미지 아이디 배열"),
                        fieldWithPath("pickOptions.secondPickOption").type(OBJECT).description("픽픽픽 두 번째 옵션 선택지"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionId").type(NUMBER)
                                .description("픽픽픽 두 번째 옵션 선택지 아이디"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionTitle").type(STRING)
                                .description("픽픽픽 두 번째 옵션 선택지 제목"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionContent").type(STRING)
                                .description("픽픽픽 두 번째 옵션 선택지 내용"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionImageIds").type(ARRAY)
                                .description("픽픽픽 두 번째 옵션 이미지 아이디 배열")
                ),
                exceptionResponseFields()
        ));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
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
        ResultActions actions = mockMvc.perform(patch("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(modifyPickRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("pick-modify-pick-option-title-bind-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                exceptionResponseFields()
        ));
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("픽픽픽을 수정할 때 픽픽픽 옵션 선택지 내용이 null 값을 허용한다.")
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
        ResultActions actions = mockMvc.perform(patch("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(modifyPickRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("pick-modify-pick-option-content",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                requestFields(
                        fieldWithPath("pickTitle").type(STRING).description("픽픽픽 타이틀"),
                        fieldWithPath("pickOptions").type(OBJECT).description("픽픽픽 옵션"),
                        fieldWithPath("pickOptions.firstPickOption").type(OBJECT).description("픽픽픽 첫 번째 옵션 선택지"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionId").type(NUMBER)
                                .description("픽픽픽 첫 번째 옵션 선택지 아이디"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionTitle").type(STRING)
                                .description("픽픽픽 첫 번째 옵션 선택지 제목"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionContent").type(NULL)
                                .description("픽픽픽 첫 번째 옵션 선택지 내용"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionImageIds").type(ARRAY)
                                .description("픽픽픽 첫 번째 옵션 이미지 아이디 배열"),
                        fieldWithPath("pickOptions.secondPickOption").type(OBJECT).description("픽픽픽 두 번째 옵션 선택지"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionId").type(NUMBER)
                                .description("픽픽픽 두 번째 옵션 선택지 아이디"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionTitle").type(STRING)
                                .description("픽픽픽 두 번째 옵션 선택지 제목"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionContent").type(NULL)
                                .description("픽픽픽 두 번째 옵션 선택지 내용"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionImageIds").type(ARRAY)
                                .description("픽픽픽 두 번째 옵션 이미지 아이디 배열")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과")
                                .attributes(authenticationType()),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pickId").type(NUMBER).description("픽픽픽 아이디")
                                .attributes(authenticationType())
                )
        ));
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
        ResultActions actions = mockMvc.perform(patch("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(modifyPickRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("pick-modify-pick-option-id-bind-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                requestFields(
                        fieldWithPath("pickTitle").type(STRING).description("픽픽픽 타이틀"),
                        fieldWithPath("pickOptions").type(OBJECT).description("픽픽픽 옵션"),
                        fieldWithPath("pickOptions.firstPickOption").type(OBJECT).description("픽픽픽 첫 번째 옵션 선택지"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionId").type(NULL)
                                .description("픽픽픽 첫 번째 옵션 선택지 아이디"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionTitle").type(STRING)
                                .description("픽픽픽 첫 번째 옵션 선택지 제목"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionContent").type(STRING)
                                .description("픽픽픽 첫 번째 옵션 선택지 내용"),
                        fieldWithPath("pickOptions.firstPickOption.pickOptionImageIds").type(ARRAY)
                                .description("픽픽픽 첫 번째 옵션 이미지 아이디 배열"),
                        fieldWithPath("pickOptions.secondPickOption").type(OBJECT).description("픽픽픽 두 번째 옵션 선택지"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionId").type(NULL)
                                .description("픽픽픽 두 번째 옵션 선택지 아이디"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionTitle").type(STRING)
                                .description("픽픽픽 두 번째 옵션 선택지 제목"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionContent").type(STRING)
                                .description("픽픽픽 두 번째 옵션 선택지 내용"),
                        fieldWithPath("pickOptions.secondPickOption.pickOptionImageIds").type(ARRAY)
                                .description("픽픽픽 두 번째 옵션 이미지 아이디 배열")
                ),
                exceptionResponseFields()
        ));
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
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());
        // docs
        actions.andDo(document("pick-detail",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰"),
                        headerWithName("Anonymous-Member-Id").optional().description("익명 회원 아이디")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),

                        fieldWithPath("data.nickname").type(STRING).description("픽픽픽 작성자 닉네임"),
                        fieldWithPath("data.userId").type(STRING).description("픽픽픽 작성자 아이디"),
                        fieldWithPath("data.pickCreatedAt").type(STRING).description("픽픽픽 생성 일시")
                                .attributes(yearMonthDateTimeType()),
                        fieldWithPath("data.pickTitle").type(STRING).description("픽픽픽 제목"),
                        fieldWithPath("data.isAuthor").type(BOOLEAN).description("현재 로그인한 회원이 픽픽픽 작성자 여부"),
                        fieldWithPath("data.isVoted").type(BOOLEAN).description("픽픽픽 투표 여부"),

                        fieldWithPath("data.pickOptions").type(OBJECT).description("픽픽픽 옵션 객체"),

                        fieldWithPath("data.pickOptions.firstPickOption").type(OBJECT).description("픽픽픽 첫번째 옵션 객체"),
                        fieldWithPath("data.pickOptions.firstPickOption.id").type(NUMBER)
                                .description("첫 번째 픽픽픽 옵션 아이디"),
                        fieldWithPath("data.pickOptions.firstPickOption.title").type(STRING)
                                .description("첫 번째 픽픽픽 옵션 제목"),
                        fieldWithPath("data.pickOptions.firstPickOption.isPicked").type(BOOLEAN)
                                .description("첫 번째 픽픽픽 옵션 투표 여부"),
                        fieldWithPath("data.pickOptions.firstPickOption.percent").type(NUMBER)
                                .description("첫 번째 픽픽픽 옵션 득표율(%)"),
                        fieldWithPath("data.pickOptions.firstPickOption.content").type(STRING)
                                .description("첫 번째 픽픽픽 옵션 내용"),
                        fieldWithPath("data.pickOptions.firstPickOption.voteTotalCount").type(NUMBER)
                                .description("첫 번째 픽픽픽 옵션 득표수"),
                        fieldWithPath("data.pickOptions.firstPickOption.pickDetailOptionImages").type(ARRAY)
                                .description("첫 번째 픽픽픽 옵션 이미지 배열"),
                        fieldWithPath("data.pickOptions.firstPickOption.pickDetailOptionImages.[].id").type(NUMBER)
                                .description("첫 번째 픽픽픽 옵션 이미지 아이디"),
                        fieldWithPath("data.pickOptions.firstPickOption.pickDetailOptionImages.[].imageUrl").type(
                                STRING).description("첫 번째 픽픽픽 옵션 이미지 url"),

                        fieldWithPath("data.pickOptions.secondPickOption").type(OBJECT).description("픽픽픽 첫번째 옵션 객체"),
                        fieldWithPath("data.pickOptions.secondPickOption.id").type(NUMBER)
                                .description("두 번째 픽픽픽 옵션 아이디"),
                        fieldWithPath("data.pickOptions.secondPickOption.title").type(STRING)
                                .description("두 번째 픽픽픽 옵션 제목"),
                        fieldWithPath("data.pickOptions.secondPickOption.isPicked").type(BOOLEAN)
                                .description("두 번째 픽픽픽 옵션 투표 여부"),
                        fieldWithPath("data.pickOptions.secondPickOption.percent").type(NUMBER)
                                .description("두 번째 픽픽픽 옵션 득표율(%)"),
                        fieldWithPath("data.pickOptions.secondPickOption.content").type(STRING)
                                .description("두 번째 픽픽픽 옵션 내용"),
                        fieldWithPath("data.pickOptions.secondPickOption.voteTotalCount").type(NUMBER)
                                .description("두 번째 픽픽픽 옵션 득표수"),
                        fieldWithPath("data.pickOptions.secondPickOption.pickDetailOptionImages").type(ARRAY)
                                .description("두 번째 픽픽픽 옵션 이미지 배열"),
                        fieldWithPath("data.pickOptions.secondPickOption.pickDetailOptionImages.[].id").type(NUMBER)
                                .description("두 번째 픽픽픽 옵션 이미지 아이디"),
                        fieldWithPath("data.pickOptions.secondPickOption.pickDetailOptionImages.[].imageUrl").type(
                                STRING).description("두 번째 픽픽픽 옵션 이미지 url")
                )
        ));
    }

    @Test
    @DisplayName("픽픽픽을 조회할 때 존재하지 않는 픽픽픽을 조회하면 예외가 발생한다.")
    void findPickDetailNotFoundException() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("pick-detail-not-found-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디")
                ),
                exceptionResponseFields()
        ));
    }

    @Test
    @DisplayName("픽픽픽을 조회할 때 승인상태가 아닌 픽픽픽을 조회하면 예외가 발생한다.")
    void findPickDetailContentStatus_REJECT() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 제목"), new Count(1), new Count(0), new Count(0), new Count(0),
                ContentStatus.REJECT, member);
        pickRepository.save(pick);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("pick-detail-reject",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디")
                ),
                exceptionResponseFields()
        ));
    }

    @Test
    @DisplayName("픽픽픽을 조회할 때 승인상태가 아닌 픽픽픽을 조회하면 예외가 발생한다.")
    void findPickDetailContentStatus_REDY() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 제목"), new Count(1), new Count(0), new Count(0), new Count(0),
                ContentStatus.READY, member);
        pickRepository.save(pick);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("pick-detail-ready",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디")
                ),
                exceptionResponseFields()
        ));
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
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/picks/vote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("vote-pick",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰"),
                        headerWithName("Anonymous-Member-Id").optional().description("익명 회원 아이디")
                ),
                requestFields(
                        fieldWithPath("pickId").type(NUMBER).description("픽픽픽 아이디"),
                        fieldWithPath("pickOptionId").type(NUMBER).description("픽픽픽 옵션 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                        fieldWithPath("data.pickId").type(NUMBER).description("픽픽픽 아이디"),
                        fieldWithPath("data.votePickOptions").type(ARRAY).description("픽픽픽 옵션 배열"),
                        fieldWithPath("data.votePickOptions.[0].pickOptionId").type(NUMBER).description("픽픽픽 옵션 아이디"),
                        fieldWithPath("data.votePickOptions.[0].pickVoteId").optional().type(NUMBER)
                                .description("픽픽픽 투표 아이디").attributes(numberOrNull()),
                        fieldWithPath("data.votePickOptions.[0].voteTotalCount").type(NUMBER)
                                .description("픽픽픽 옵션 총 득표수"),
                        fieldWithPath("data.votePickOptions.[0].percent").type(NUMBER).description("픽픽픽 옵션 득표율(%)"),
                        fieldWithPath("data.votePickOptions.[0].isPicked").type(BOOLEAN)
                                .description("회원의 픽픽픽 옵션 선택 여부"),
                        fieldWithPath("data.votePickOptions.[1].pickOptionId").type(NUMBER).description("픽픽픽 옵션 아이디"),
                        fieldWithPath("data.votePickOptions.[1].pickVoteId").optional().type(NUMBER)
                                .description("픽픽픽 투표 아이디").attributes(numberOrNull()),
                        fieldWithPath("data.votePickOptions.[1].voteTotalCount").type(NUMBER)
                                .description("픽픽픽 옵션 총 득표수"),
                        fieldWithPath("data.votePickOptions.[1].percent").type(NUMBER).description("픽픽픽 옵션 득표율(%)"),
                        fieldWithPath("data.votePickOptions.[1].isPicked").type(BOOLEAN).description("회원의 픽픽픽 옵션 선택 여부")
                )
        ));
    }

    @Test
    @DisplayName("픽픽픽 옵션에 투표할 때 pickId와 pcikOptionId가 null 이면 예외가 발생한다.")
    void votePickOptionBindException() throws Exception {
        // given
        VotePickOptionRequest request = VotePickOptionRequest.builder()
                .pickId(null)
                .pickOptionId(null)
                .build();

        // when // then
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/picks/vote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("vote-pick-bind-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                exceptionResponseFields()
        ));
    }

    @Test
    @DisplayName("회원이 픽픽픽 옵션을 투표할 때 이미 투표한 픽픽픽 옵션에 투표를 하면 예외가 발생한다.")
    void votePickOption_INVALID_CAN_NOT_VOTE_SAME_PICK_OPTION_MESSAGE() throws Exception {
        // given
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

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(member, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        VotePickOptionRequest request = VotePickOptionRequest.builder()
                .pickId(pick.getId())
                .pickOptionId(firstPickOption.getId())
                .build();

        // when // then
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/picks/vote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("vote-pick-option-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                exceptionResponseFields()
        ));
    }

    @Test
    @DisplayName("회원이 픽픽픽 옵션을 투표할 때 픽픽픽이 없으면 예외가 발생한다.")
    void votePickOption_INVALID_NOT_FOUND_PICK_MESSAGE() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        VotePickOptionRequest request = VotePickOptionRequest.builder()
                .pickId(0L)
                .pickOptionId(0L)
                .build();

        // when // then
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/picks/vote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("vote-pick-not-found-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                exceptionResponseFields()
        ));
    }

    @Test
    @DisplayName("익명 회원이 픽픽픽 옵션을 투표할 때 픽픽픽이 없으면 예외가 발생한다.")
    void votePickOption_INVALID_ANONYMOUS_MEMBER_ID_MESSAGE() throws Exception {
        // given
        VotePickOptionRequest request = VotePickOptionRequest.builder()
                .pickId(0L)
                .pickOptionId(0L)
                .build();

        // when // then
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/picks/vote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("vote-pick-invalid-anonymous-member-id",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                exceptionResponseFields()
        ));
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
        PickOption firstPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 이미지 생성
        PickOptionImage firstPickOptionImage = createPickOptionImage("firstPickOptionImage", firstPickOption);
        PickOptionImage secondPickOptionImage = createPickOptionImage("secondPickOptionImage", firstPickOption);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(member, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        // when // then
        ResultActions actions = mockMvc.perform(delete("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("delete-pick",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디")
                ),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과")
                )
        ));
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
                "흑빛파티시엘", "1234", "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), author);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 이미지 생성
        PickOptionImage firstPickOptionImage = createPickOptionImage("firstPickOptionImage", firstPickOption);
        PickOptionImage secondPickOptionImage = createPickOptionImage("secondPickOptionImage", secondPickOption);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(author, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        // when // then
        ResultActions actions = mockMvc.perform(delete("/devdevdev/api/v1/picks/{pickId}", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("delete-pick-not-found-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디")
                ),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                exceptionResponseFields()
        ));
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

        Pick targetPick = createPick(new Title("나 유소영 일본 간다!"), new Count(1), new Count(1), member,
                ContentStatus.APPROVAL,
                List.of(1.0, 1.0, 1.0));
        Pick pick1 = createPick(new Title("쏘영쏘 일본 진짜 대박 짱 부럽다!!!"), new Count(2), new Count(5), member,
                ContentStatus.APPROVAL,
                List.of(0.1, 0.2, 0.3));
        Pick pick2 = createPick(new Title("쏘영님 일본 진짜 부럽다.."), new Count(3), new Count(4), member,
                ContentStatus.APPROVAL,
                List.of(0.2, 0.3, 0.4));
        Pick pick3 = createPick(new Title("소영님 일본 부럽다."), new Count(4), new Count(3), member,
                ContentStatus.APPROVAL,
                List.of(0.3, 0.4, 0.5));
        Pick pick4 = createPick(new Title("나도 일본 가고 싶따....ㅠㅠ"), new Count(5), new Count(2), member, ContentStatus.READY,
                List.of(0.4, 0.5, 0.6));
        Pick pick5 = createPick(new Title("일본 맛있는거 진짜 많겠찌...? ㅠ0ㅠ"), new Count(6), new Count(1), member,
                ContentStatus.REJECT,
                List.of(0.4, 0.5, 0.6));
        pickRepository.saveAll(List.of(targetPick, pick1, pick2, pick3, pick4, pick5));

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}/similarties", targetPick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("pick-similarity",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("pickId").description("타겟 픽픽픽 아이디")
                ),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("datas").type(ARRAY).description("응답 데이터"),
                        fieldWithPath("datas.[].id").type(NUMBER).description("픽픽픽 아이디"),
                        fieldWithPath("datas.[].title").type(STRING).description("픽픽픽 제목"),
                        fieldWithPath("datas.[].voteTotalCount").type(NUMBER).description("픽픽픽 총 투표 수"),
                        fieldWithPath("datas.[].commentTotalCount").type(NUMBER).description("픽픽픽 총 댓글 수"),
                        fieldWithPath("datas.[].similarity").type(NUMBER).description("타겟 픽픽픽과 유사도 점수")
                )
        ));
    }

    @Test
    @DisplayName("나도 고민했는데 픽픽픽을 조회할 때 타겟 픽픽픽이 존재하지 않으면 예외가 발생한다.")
    void getSimilarPicks_NOT_FOUND() throws Exception {
        // given // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}/similarties", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("pick-similarity-not-found",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("pickId").description("타겟 픽픽픽 아이디")
                ),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                exceptionResponseFields()
        ));
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
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}/similarties", targetPick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("pick-similarity-bad-request",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("pickId").description("타겟 픽픽픽 아이디")
                ),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                exceptionResponseFields()
        ));
    }

    @Test
    @DisplayName("나도 고민했는데 픽픽픽을 조회할 때 타겟 픽픽픽의 embeddings 값이 없으면 예외가 발생한다.")
    void getSimilarPicks_INTERNAL_SERVER_EXCEPTION() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 임베딩 값이 없는 타겟 픽픽픽
        Pick targetPick = createPick(new Title("유소영"), new Count(1), new Count(1), member, ContentStatus.APPROVAL,
                List.of());

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
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}/similarties", targetPick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is5xxServerError());

        // docs
        actions.andDo(document("pick-similarity-internal-server-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("pickId").description("타겟 픽픽픽 아이디")
                ),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                exceptionResponseFields()
        ));
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
        return PickVote.builder()
                .member(member)
                .pickOption(pickOption)
                .pick(pick)
                .build();
    }

    private Pick createPick(Title title, Count count, Member member) {
        return Pick.builder()
                .title(title)
                .voteTotalCount(count)
                .member(member)
                .build();
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
                .imageUrl("imageUrl")
                .imageKey("imageKey")
                .build();
    }

    private PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents, Count count,
                                        PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(count)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private PickOptionImage createPickOptionImage(String name, PickOption pickOption) {
        PickOptionImage pickOptionImage = PickOptionImage.builder()
                .name(name)
                .imageUrl("imageUrl")
                .imageKey("imageKey")
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

    private MockMultipartFile createMockMultipartFile(String name, String originalFilename, String mediaType) {
        return new MockMultipartFile(
                name,
                originalFilename,
                mediaType,
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

    private Pick createPick(Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                            Count pickcommentTotalCount, String thumbnailUrl, String author,
                            ContentStatus contentStatus,
                            List<PickOption> pickOptions, List<PickVote> pickVotes
    ) {

        Pick pick = Pick.builder()
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

    private PickOption createPickOption(Title title, PickOptionContents pickOptionContents, Count voteTotalCount) {
        return PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(voteTotalCount)
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

    private PickOptionImage createPickOptionImage(String imageUrl, String imageKey) {
        return PickOptionImage.builder()
                .name("imageName")
                .imageUrl(imageUrl)
                .imageKey(imageKey)
                .build();
    }
}
