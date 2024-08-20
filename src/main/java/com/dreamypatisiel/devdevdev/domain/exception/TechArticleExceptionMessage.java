package com.dreamypatisiel.devdevdev.domain.exception;

public class TechArticleExceptionMessage {
    public static final String NOT_FOUND_ELASTIC_ID_MESSAGE = "기술블로그와 연결된 엘라스틱 데이터가 없습니다.";
    public static final String NOT_FOUND_TECH_ARTICLE_MESSAGE = "존재하지 않는 기술블로그입니다.";
    public static final String NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE = "존재하지 않는 기술블로그입니다.";
    public static final String INVALID_ELASTIC_METHODS_CALL_MESSAGE = "검색어가 없습니다. 검색어를 입력해주세요.";
    public static final String NOT_FOUND_CURSOR_SCORE_MESSAGE = "정확도순 페이지네이션을 위한 커서의 score를 입력해주세요.";
    public static final String KEYWORD_WITH_SPECIAL_SYMBOLS_EXCEPTION_MESSAGE = "검색어에 특수문자는 포함할 수 없어요";
    public static final String INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE = "존재하지 않는 기술블로그 댓글입니다.";
    public static final String INVALID_CAN_NOT_REPLY_DELETED_TECH_COMMENT_MESSAGE = "삭제된 기술블로그 댓글에는 답글을 작성할 수 없습니다.";
}
