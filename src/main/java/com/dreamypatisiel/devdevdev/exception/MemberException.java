package com.dreamypatisiel.devdevdev.exception;

public class MemberException extends IllegalArgumentException {

    public static final String INVALID_MEMBER_NOT_FOUND_MESSAGE = "회원을 찾을 수 없습니다.";

    public MemberException(String s) {
        super(s);
    }
}
