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

        @Override
        public String createLimiterKey(HttpServletRequest request) {
            String ip = request.getRemoteAddr();
            try {
                String gaId = CookieUtils.getRequestCookieValueByName(request, "_ga");
                return gaId + DASH + ip;
            } catch (CookieException e) {
                return ip;
            }
        }
    },
    TEST {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(1_000, Refill.intervally(1_000, Duration.ofMinutes(1)));

        }

        @Override
        public String createLimiterKey(HttpServletRequest request) {
            String ip = request.getRemoteAddr();
            try {
                String gaId = CookieUtils.getRequestCookieValueByName(request, "_ga");
                return gaId + DASH + ip;
            } catch (CookieException e) {
                return ip;
            }
        }
    },
    LOCAL {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(5, Refill.intervally(5, Duration.ofSeconds(10)));
        }

        @Override
        public String createLimiterKey(HttpServletRequest request) {
            String ip = request.getRemoteAddr();
            try {
                String gaId = CookieUtils.getRequestCookieValueByName(request, "_ga");
                return gaId + DASH + ip;
            } catch (CookieException e) {
                return ip;
            }
        }
    },
    DEV {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(120, Refill.intervally(120, Duration.ofMinutes(1)));
        }

        @Override
        public String createLimiterKey(HttpServletRequest request) {
            String ip = request.getRemoteAddr();
            try {
                String gaId = CookieUtils.getRequestCookieValueByName(request, "_ga");
                return gaId + DASH + ip;
            } catch (CookieException e) {
                return ip;
            }
        }
    },
    PROD {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(120, Refill.intervally(120, Duration.ofMinutes(1)));
        }

        @Override
        public String createLimiterKey(HttpServletRequest request) {
            String ip = request.getRemoteAddr();
            try {
                String gaId = CookieUtils.getRequestCookieValueByName(request, "_ga");
                return gaId + DASH + ip;
            } catch (CookieException e) {
                return ip;
            }
        }
    };

    public static final String DASH = "-";

    public abstract Bandwidth getLimit();

    /**
     * @Note: 현재 모든 정책에 _ga와 ip를 조합하여 key를 생성한다. <br/> _ga의 쿠키가 없으면 ip로 key를 생성한다.
     * @Author: 장세웅
     * @Since: 2024.06.26
     */
    public abstract String createLimiterKey(HttpServletRequest request);

    public static Bandwidth resolvePlan(String targetPlan) {
        return getLimiterPlan(targetPlan).getLimit();
    }

    public static LimiterPlan getLimiterPlan(String targetPlan) {
        return Arrays.stream(LimiterPlan.values())
                .filter(limiterPlan -> limiterPlan.name().equalsIgnoreCase(targetPlan))
                .findAny()
                .orElseGet(() -> DEFAULT);
    }
}
