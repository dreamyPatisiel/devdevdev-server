package com.dreamypatisiel.devdevdev.limiter;

import com.dreamypatisiel.devdevdev.exception.CookieException;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LimiterPlan {

    DEFAULT {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(120, Refill.intervally(120, Duration.ofMinutes(1)));
        }
    },
    TEST {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(1_000, Refill.intervally(1_000, Duration.ofSeconds(1)));

        }
    },
    LOCAL {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(5, Refill.intervally(5, Duration.ofSeconds(10)));
        }
    },
    DEV {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(120, Refill.intervally(120, Duration.ofMinutes(1)));
        }
    },
    PROD {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(120, Refill.intervally(120, Duration.ofMinutes(1)));
        }
    };

    public static final String DASH = "-";

    public abstract Bandwidth getLimit();

    public static Bandwidth resolvePlan(String targetPlan) {
        return Arrays.stream(LimiterPlan.values())
                .filter(limiterPlan -> limiterPlan.name().equalsIgnoreCase(targetPlan))
                .map(LimiterPlan::getLimit)
                .findFirst()
                .orElseGet(DEFAULT::getLimit);
    }

    /**
     * @Note: 기본적으로 _ga와 ip를 조합하여 key를 생성한다. <br/> _ga의 쿠키가 없으면 ip로 key를 생성한다.
     * @Author: 장세웅
     * @Since: 2024.06.26
     */
    public static String createLimiterKey(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        try {
            String gaId = CookieUtils.getRequestCookieValueByName(request, "_ga");
            return gaId + DASH + ip;
        } catch (CookieException e) {
            return ip;
        }
    }
}
