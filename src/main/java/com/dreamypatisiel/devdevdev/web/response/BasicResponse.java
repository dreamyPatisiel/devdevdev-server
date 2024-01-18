package com.dreamypatisiel.devdevdev.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // null 인 필드는 제외
public class BasicResponse<T> {
    private ResultType resultType;
    private String message;
    private T data;
    private int errorCode;

    private BasicResponse(ResultType resultType, String message, int errorCode) {
        this.resultType = resultType;
        this.message = message;
        this.errorCode = errorCode;
    }

    private BasicResponse(ResultType resultType, T data) {
        this.resultType = resultType;
        this.data = data;
    }

    public static <T> BasicResponse<T> success(T data) {
        return new BasicResponse<>(ResultType.SUCCESS, data);
    }

    public static <T> BasicResponse<T> fail(String message, int errorCode) {
        return new BasicResponse<>(ResultType.FAIL, message, errorCode);
    }
}
