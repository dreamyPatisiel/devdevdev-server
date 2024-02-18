package com.dreamypatisiel.devdevdev.global.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtils {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    public static final int DEFAULT_SCALE = 4;


    // 퍼센트 계산
    public static BigDecimal toPercentageOf(BigDecimal value, BigDecimal total) {
        return value.divide(total, DEFAULT_SCALE, RoundingMode.HALF_UP).multiply(ONE_HUNDRED);
    }

    // 퍼센트의 값 계산
    public static BigDecimal percentOf(BigDecimal percentage, BigDecimal total) {
        return percentage.multiply(total).divide(ONE_HUNDRED, DEFAULT_SCALE, RoundingMode.HALF_UP);
    }
}
