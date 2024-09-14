package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.entity.BlameType;
import com.dreamypatisiel.devdevdev.domain.repository.BlameRepository;
import com.dreamypatisiel.devdevdev.domain.repository.BlameTypeRepository;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

public class BlameControllerDocsTest extends SupportControllerDocsTest {

    @Autowired
    BlameTypeRepository blameTypeRepository;
    @Autowired
    BlameRepository blameRepository;

    @Test
    @DisplayName("회원이 신고 사유를 조회한다.")
    void getBlames() throws Exception {
        // given
        BlameType blameType = createBlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/blames")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("get-blame",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("datas").type(ARRAY).description("응답 데이터"),
                        fieldWithPath("datas.[].id").type(NUMBER).description("신고 종류 아이디"),
                        fieldWithPath("datas.[].reason").type(STRING).description("신고 사유"),
                        fieldWithPath("datas.[].sortOrder").type(NUMBER).description("정렬 순서")
                )
        ));
    }

    @Test
    @DisplayName("회원이 아니면 신고사유를 조회할 때 예외가 발생한다.")
    void getBlamesAuthorizationException() throws Exception {
        // given
        BlameType blameType = createBlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/blames")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        // docs
        actions.andDo(document("get-blame-authorization-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("message").type(STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(NUMBER).description("에러 코드")
                )
        ));
    }

    private BlameType createBlameType(String reason, int sortOrder) {
        return new BlameType(reason, sortOrder);
    }
}
