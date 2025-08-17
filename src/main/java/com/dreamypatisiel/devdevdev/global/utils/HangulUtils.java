package com.dreamypatisiel.devdevdev.global.utils;

/**
 * 한글 처리를 위한 유틸리티 클래스
 */
public abstract class HangulUtils {

    // 한글 유니코드 범위
    private static final int HANGUL_START = 0xAC00; // '가'
    private static final int HANGUL_END = 0xD7A3;   // '힣'

    // 자모 유니코드 범위
    private static final int JAMO_START = 0x1100;   // 'ㄱ'
    private static final int JAMO_END = 0x11FF;     // 'ㅿ'

    // 호환 자모 유니코드 범위
    private static final int COMPAT_JAMO_START = 0x3130; // 'ㄱ'
    private static final int COMPAT_JAMO_END = 0x318F;   // 'ㆎ'

    // 한글 분해를 위한 상수
    private static final int CHOSUNG_COUNT = 19;
    private static final int JUNGSUNG_COUNT = 21;
    private static final int JONGSUNG_COUNT = 28;

    // 초성 배열
    private static final char[] CHOSUNG = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    // 중성 배열
    private static final char[] JUNGSUNG = {
            'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
            'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    };

    // 종성 배열 (첫 번째는 받침 없음)
    private static final char[] JONGSUNG = {
            '\0', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
            'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    /**
     * 문자열에 한글이 포함되어 있는지 확인
     */
    public static boolean hasHangul(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (char ch : text.toCharArray()) {
            if (isHangul(ch)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 한글 문자열을 자모로 분해
     */
    public static String convertToJamo(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();

        for (char ch : text.toCharArray()) {
            if (isCompleteHangul(ch)) {
                // 완성된 한글 문자를 자모로 분해
                int unicode = ch - HANGUL_START;

                int chosungIndex = unicode / (JUNGSUNG_COUNT * JONGSUNG_COUNT);
                int jungsungIndex = (unicode % (JUNGSUNG_COUNT * JONGSUNG_COUNT)) / JONGSUNG_COUNT;
                int jongsungIndex = unicode % JONGSUNG_COUNT;

                result.append(CHOSUNG[chosungIndex]);
                result.append(JUNGSUNG[jungsungIndex]);

                if (jongsungIndex > 0) {
                    result.append(JONGSUNG[jongsungIndex]);
                }
            } else {
                // 한글이 아니거나 이미 자모인 경우 그대로 추가
                result.append(ch);
            }
        }

        return result.toString();
    }

    /**
     * 한글 문자열에서 초성만 추출
     */
    public static String extractChosung(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();

        for (char ch : text.toCharArray()) {
            if (isCompleteHangul(ch)) {
                // 완성된 한글 문자에서 초성 추출
                int unicode = ch - HANGUL_START;
                int chosungIndex = unicode / (JUNGSUNG_COUNT * JONGSUNG_COUNT);
                result.append(CHOSUNG[chosungIndex]);
            } else if (isChosung(ch)) {
                // 이미 초성인 경우 그대로 추가
                result.append(ch);
            } else if (!isHangul(ch)) {
                // 한글이 아닌 문자는 그대로 추가
                result.append(ch);
            }
            // 중성, 종성은 무시
        }

        return result.toString();
    }

    /**
     * 문자가 한글인지 확인 (완성형 한글 + 자모)
     */
    private static boolean isHangul(char ch) {
        return isCompleteHangul(ch) || isJamo(ch) || isCompatJamo(ch);
    }

    /**
     * 문자가 완성된 한글인지 확인
     */
    private static boolean isCompleteHangul(char ch) {
        return ch >= HANGUL_START && ch <= HANGUL_END;
    }

    /**
     * 문자가 자모인지 확인
     */
    private static boolean isJamo(char ch) {
        return ch >= JAMO_START && ch <= JAMO_END;
    }

    /**
     * 문자가 호환 자모인지 확인
     */
    private static boolean isCompatJamo(char ch) {
        return ch >= COMPAT_JAMO_START && ch <= COMPAT_JAMO_END;
    }

    /**
     * 문자가 초성인지 확인
     */
    private static boolean isChosung(char ch) {
        for (char chosung : CHOSUNG) {
            if (ch == chosung) {
                return true;
            }
        }
        return false;
    }
}
