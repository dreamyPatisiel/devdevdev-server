package com.dreamypatisiel.devdevdev.web.docs.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.ResponseCookie;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;

public class CustomPreprocessors {

    public static OperationPreprocessor modifyResponseBody() {
        return new OperationPreprocessor() {

            private final ObjectMapper objectMapper = new ObjectMapper()
                    .disable(SerializationFeature.INDENT_OUTPUT); // 한 줄 JSON

            @Override
            public OperationRequest preprocess(OperationRequest request) {
                return request;
            }

            @Override
            public OperationResponse preprocess(OperationResponse response) {
                try {
                    String original = response.getContentAsString();

                    // data: 로 시작하는 줄만 추출
                    List<String> formattedLines = Arrays.stream(original.split("\n"))
                            .filter(line -> line.startsWith("data:"))
                            .map(line -> {
                                String json = line.replaceFirst("data:", "").trim();
                                try {
                                    Object parsed = objectMapper.readValue(json, Object.class);
                                    String oneLineJson = objectMapper.writeValueAsString(parsed);

                                    // 보기 좋게 : 앞에 공백 추가
                                    oneLineJson = oneLineJson.replaceAll("\":\"", "\" : \"")
                                            .replaceAll("\":", "\" : ")
                                            .replaceAll(",\"", ", \"");

                                    return "data: " + oneLineJson;
                                } catch (Exception e) {
                                    return line; // 실패하면 원본 유지
                                }
                            })
                            .collect(Collectors.toList());

                    String formatted = String.join("\n", formattedLines);
                    byte[] contentBytes = formatted.getBytes(StandardCharsets.UTF_8);

                    return new OperationResponse() {
                        @Override
                        public HttpStatusCode getStatus() {
                            return response.getStatus();
                        }

                        @Override
                        public HttpHeaders getHeaders() {
                            return response.getHeaders();
                        }

                        @Override
                        public byte[] getContent() {
                            return contentBytes;
                        }

                        @Override
                        public String getContentAsString() {
                            return formatted;
                        }

                        @Override
                        public Collection<ResponseCookie> getCookies() {
                            return response.getCookies();
                        }
                    };

                } catch (Exception e) {
                    throw new RuntimeException("SSE 응답 포매팅 실패", e);
                }
            }
        };
    }
}
