package com.dreamypatisiel.devdevdev.domain.service.response.util;

public class CommonResponseUtil {
    public static String sliceAndMaskEmail(String email) {
        String id = email.replaceAll("@.*", ""); // @ 이후 문자열 제거
        return id.replaceAll("(?<=.{3}).", "*"); // 앞 3자리만 제외하고 전부 *로 마스킹
    }
}
