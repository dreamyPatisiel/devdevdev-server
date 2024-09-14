package com.dreamypatisiel.devdevdev.web.controller;

import static com.dreamypatisiel.devdevdev.web.dto.response.ResultType.SUCCESS;
import static io.lettuce.core.BitFieldArgs.OverflowType.FAIL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

public class BlameControllerTest extends SupportControllerTest {

    @Autowired
    BlameTypeRepository blameTypeRepository;
    @Autowired
    BlameRepository blameRepository;

    @Test
    @DisplayName("회원이 신고사유를 조회한다.")
    void getBlames() throws Exception {
        // given
        BlameType blameType = createBlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/blames")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
                .andExpect(jsonPath("$.datas").isNotEmpty())
                .andExpect(jsonPath("$.datas.[0].id").isNumber())
                .andExpect(jsonPath("$.datas.[0].reason").isString())
                .andExpect(jsonPath("$.datas.[0].sortOrder").isNumber());
    }

    @Test
    @DisplayName("회원이 아니면 신고사유를 조회할 때 예외가 발생한다.")
    void getBlamesException() throws Exception {
        // given
        BlameType blameType = createBlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/blames")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").isNumber());
    }

    private BlameType createBlameType(String reason, int sortOrder) {
        return new BlameType(reason, sortOrder);
    }
}
