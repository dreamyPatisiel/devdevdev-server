package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.authenticationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.pickOptionImageNameType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.pickSortType;
import static com.dreamypatisiel.devdevdev.web.response.ResultType.SUCCESS;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import com.dreamypatisiel.devdevdev.domain.repository.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickService;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
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
        String thumbnailUrl = "https://devdevdev.co.kr/devdevdev/api/v1/picks/image/tumbnail/1";
        String author = "운영자";
        Pick pick = createPick(title, count, count, count, thumbnailUrl,
                author, List.of(pickOption1, pickOption2), List.of());
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
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                queryParameters(
                    parameterWithName("pickId").optional().description("픽픽픽 아이디"),
                    parameterWithName("pickSort").optional().description("픽픽픽 정렬 조건").attributes(pickSortType()),
                    parameterWithName("size").optional().description("조회되는 데이터 수")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),

                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("픽픽픽 메인 배열"),
                        fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("픽픽픽 아이디"),
                        fieldWithPath("data.content[].title").type(JsonFieldType.STRING).description("픽픽픽 제목"),
                        fieldWithPath("data.content[].voteTotalCount").type(JsonFieldType.NUMBER).description("픽픽픽 전체 투표 수"),
                        fieldWithPath("data.content[].commentTotalCount").type(JsonFieldType.NUMBER).description("픽픽픽 전체 댓글 수"),
                        fieldWithPath("data.content[].viewTotalCount").type(JsonFieldType.NUMBER).description("픽픽픽 조회 수"),
                        fieldWithPath("data.content[].popularScore").type(JsonFieldType.NUMBER).description("픽픽픽 인기점수"),
                        fieldWithPath("data.content[].isVoted").attributes(authenticationType()).type(JsonFieldType.BOOLEAN).description("픽픽픽 투표 여부(익명 사용자는 필드가 없다.)"),

                        fieldWithPath("data.content[].pickOptions").type(JsonFieldType.ARRAY).description("픽픽픽 옵션 배열"),
                        fieldWithPath("data.content[].pickOptions[].id").type(JsonFieldType.NUMBER).description("픽픽픽 옵션 아이디"),
                        fieldWithPath("data.content[].pickOptions[].title").type(JsonFieldType.STRING).description("픽픽픽 옵션 제목"),
                        fieldWithPath("data.content[].pickOptions[].percent").type(JsonFieldType.NUMBER).description("픽픽픽 옵션 투표율(%)"),
                        fieldWithPath("data.content[].pickOptions[].isPicked").attributes(authenticationType()).type(JsonFieldType.BOOLEAN).description("픽픽픽 옵션 투표 여부(익명 사용자는 필드가 없다.)"),
                        fieldWithPath("data.content[].pickOptions[].id").type(JsonFieldType.NUMBER).description("픽픽픽 옵션 아이디"),
                        fieldWithPath("data.content[].pickOptions[].title").type(JsonFieldType.STRING).description("픽픽픽 옵션 제목"),
                        fieldWithPath("data.content[].pickOptions[].percent").type(JsonFieldType.NUMBER).description("픽픽픽 옵션 투표율(%)"),
                        fieldWithPath("data.content[].pickOptions[].isPicked").attributes(authenticationType()).type(JsonFieldType.BOOLEAN).description("픽픽픽 옵션 투표 여부(익명 사용자는 필드가 없다.)"),

                        fieldWithPath("data.pageable").type(JsonFieldType.OBJECT).description("픽픽픽 메인 페이지네이션 정보"),
                        fieldWithPath("data.pageable.pageNumber").type(JsonFieldType.NUMBER).description("페이지 번호"),
                        fieldWithPath("data.pageable.pageSize").type(JsonFieldType.NUMBER).description("페이지 사이즈"),

                        fieldWithPath("data.pageable.sort").type(JsonFieldType.OBJECT).description("정렬 정보"),
                        fieldWithPath("data.pageable.sort.empty").type(JsonFieldType.BOOLEAN).description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.pageable.sort.sorted").type(JsonFieldType.BOOLEAN).description("정렬 여부"),
                        fieldWithPath("data.pageable.sort.unsorted").type(JsonFieldType.BOOLEAN).description("비정렬 여부"),

                        fieldWithPath("data.pageable.offset").type(JsonFieldType.NUMBER).description("페이지 오프셋 (페이지 크기 * 페이지 번호)"),
                        fieldWithPath("data.pageable.paged").type(JsonFieldType.BOOLEAN).description("페이지 정보 포함 여부"),
                        fieldWithPath("data.pageable.unpaged").type(JsonFieldType.BOOLEAN).description("페이지 정보 비포함 여부"),

                        fieldWithPath("data.first").type(JsonFieldType.BOOLEAN).description("현재 페이지가 첫 페이지 여부"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("현재 페이지가 마지막 페이지 여부"),
                        fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.number").type(JsonFieldType.NUMBER).description("현재 페이지"),

                        fieldWithPath("data.sort").type(JsonFieldType.OBJECT).description("정렬 정보"),
                        fieldWithPath("data.sort.empty").type(JsonFieldType.BOOLEAN).description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.sort.sorted").type(JsonFieldType.BOOLEAN).description("정렬 상태 여부"),
                        fieldWithPath("data.sort.unsorted").type(JsonFieldType.BOOLEAN).description("비정렬 상태 여부"),
                        fieldWithPath("data.numberOfElements").type(JsonFieldType.NUMBER).description("현재 페이지 데이터 수"),
                        fieldWithPath("data.empty").type(JsonFieldType.BOOLEAN).description("현재 빈 페이지 여부")
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
        when(amazonS3Client.getUrl(anyString(), anyString())).thenReturn(new URL("http", "localhost", 8080, "/xxx.png"));

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
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과")
                                .attributes(authenticationType()),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pickOptionImages").type(JsonFieldType.ARRAY).description("픽픽픽 옵션 이미지 배열")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pickOptionImages[].name").type(JsonFieldType.STRING).description("픽픽픽 옵션 이미지 이름")
                                .attributes(pickOptionImageNameType(), authenticationType()),
                        fieldWithPath("data.pickOptionImages[].pickOptionImageId").type(JsonFieldType.NUMBER).description("픽픽픽 옵션 이미지 아이디")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pickOptionImages[].imageUrl").type(JsonFieldType.STRING).description("픽픽픽 옵션 이미지 URL")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pickOptionImages[].imageKey").type(JsonFieldType.STRING).description("픽픽픽 옵션 이미지 KEY(경로)")
                                .attributes(authenticationType())
                )
        ));
    }

    @Test
    @DisplayName("비회원은 픽픽픽 이미지를 업로드에 실패한다.")
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
        when(amazonS3Client.getUrl(anyString(), anyString())).thenReturn(new URL("http", "localhost", 8080, "/xxx.png"));

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
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
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
        when(amazonS3Client.getUrl(anyString(), anyString())).thenReturn(new URL("http", "localhost", 8080, "/xxx.gif"));

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
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
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
        ResultActions actions = mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/devdevdev/api/v1/picks/image/{pickOptionImageId}"
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
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과")
                                .attributes(authenticationType())
                )
        ));
    }

    @Test
    @DisplayName("회원은 픽픽픽 옵션에 대한 이미지를 삭제할 수 있다.")
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
                        RestDocumentationRequestBuilders.delete("/devdevdev/api/v1/picks/image/{pickOptionImageId}"
                                        , pickOptionImage.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        // docs
        actions.andDo(document("pick-main-option-delete-image-anonymous-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("pickOptionImageId").description("픽픽픽 옵션 이미지 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
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
                        RestDocumentationRequestBuilders.delete("/devdevdev/api/v1/picks/image/{pickOptionImageId}", 1L)
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
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
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

    private MockMultipartFile createMockMultipartFile(String name, String originalFilename, String mediaTypeValue) {
        return new MockMultipartFile(
                name,
                originalFilename,
                mediaTypeValue,
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
                            Count pickcommentTotalCount, String thumbnailUrl,
                            String author, List<PickOption> pickOptions, List<PickVote> pickVotes
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
