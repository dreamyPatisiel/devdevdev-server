package com.dreamypatisiel.devdevdev.web.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // null 인 필드는 제외
public class BasicResponse<T> {
    private ResultType resultType;
    private String message;
    private T data;
    @JsonAlias(value = "data")
    private List<T> datas;
    private Integer errorCode;

    private BasicResponse(ResultType resultType, String message, int errorCode) {
        this.resultType = resultType;
        this.message = message;
        this.errorCode = errorCode;
    }

    private BasicResponse(ResultType resultType, T data) {
        this.resultType = resultType;
        this.data = data;
    }

    private BasicResponse(ResultType resultType, List<T> datas) {
        this.resultType = resultType;
        this.datas = datas;
    }

    private BasicResponse(ResultType resultType) {
        this.resultType = resultType;
    }

    public static <T> BasicResponse<T> success() {
        return new BasicResponse<>(ResultType.SUCCESS);
    }

    public static <T> BasicResponse<T> success(T data) {
        return new BasicResponse<>(ResultType.SUCCESS, data);
    }

    public static <T> BasicResponse<T> success(List<T> datas) {
        return new BasicResponse<>(ResultType.SUCCESS, datas);
    }

    public static <T> BasicResponse<T> fail(String message, int errorCode) {
        return new BasicResponse<>(ResultType.FAIL, message, errorCode);
    }
}
