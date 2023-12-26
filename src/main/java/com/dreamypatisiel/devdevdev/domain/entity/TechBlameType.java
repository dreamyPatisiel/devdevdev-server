package com.dreamypatisiel.devdevdev.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TechBlameType {

    /**
     * PROFANITY AND HATE SPEECH:
     * 해당 댓글에 욕설, 비하, 차별적인 언어가 사용되었을 경우 신고할 수 있도록 합니다.
     *
     * SPAM OR ADVERTISEMENT:
     * 댓글이 스팸이나 상업적 광고를 목적으로 하는 경우 신고할 수 있도록 합니다.
     *
     * FLOODING/REPETITION:
     * 사용자가 동일한 내용을 반복적으로 게시하는 경우를 신고할 수 있도록 합니다.
     *
     * ILLEGAL CONTENT:
     * 불법적인 활동이나 컨텐츠를 게시한 경우를 신고할 수 있도록 합니다.
     *
     * DISRESPECTFUL BEHAVIOR WITHOUT PROFANITY:
     * 상대방에 대한 무례한 행동이나 비방적인 내용이 포함된 경우를 신고할 수 있도록 합니다.
     *
     * PERSONAL INFORMATION DISCLOSURE:
     * 다른 사용자의 개인 정보를 노출하는 경우를 신고할 수 있도록 합니다.
     *
     * INAPPROPRIATE CONTENT:
     * 선정적이거나 부적절한 콘텐츠가 포함된 댓글을 신고할 수 있도록 합니다.
     *
     * FAKE ACCOUNTS OR SCAM:
     * 가짜 계정이나 사기적인 행위를 의심하는 경우를 신고할 수 있도록 합니다.
     * */
    PROFANITY_AND_HATE_SPEECH("해당 댓글에 욕설, 비하, 차별적인 언어가 사용되었을 경우 신고할 수 있도록 합니다."),
    SPAM_OR_ADVERTISEMENT("댓글이 스팸이나 상업적 광고를 목적으로 하는 경우 신고할 수 있도록 합니다."),
    FLOODING_REPETITION("사용자가 동일한 내용을 반복적으로 게시하는 경우를 신고할 수 있도록 합니다."),
    ILLEGAL_CONTENT("불법적인 활동이나 컨텐츠를 게시한 경우를 신고할 수 있도록 합니다."),
    DISRESPECTFUL_BEHAVIOR_WITHOUT_PROFANITY("상대방에 대한 무례한 행동이나 비방적인 내용이 포함된 경우를 신고할 수 있도록 합니다."),
    PERSONAL_INFORMATION_DISCLOSURE("다른 사용자의 개인 정보를 노출하는 경우를 신고할 수 있도록 합니다."),
    INAPPROPRIATE_CONTENT("선정적이거나 부적절한 콘텐츠가 포함된 댓글을 신고할 수 있도록 합니다."),
    FAKE_ACCOUNTS_OR_SCAM("가짜 계정이나 사기적인 행위를 의심하는 경우를 신고할 수 있도록 합니다.");

    private final String message;

}
