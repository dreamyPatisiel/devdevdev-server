package com.dreamypatisiel.devdevdev.global.common;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class TimeProvider {

    public final static String DEFAULT_ZONE_ID = "Asia/Seoul";

    public LocalDateTime getLocalDateTimeNow() {
        return ZonedDateTime.now(ZoneId.of(DEFAULT_ZONE_ID)).toLocalDateTime();
    }

    public Date getDateNow() {
        return new Date();
    }

    public Date getDateNowByInstant(Instant instant) {
        return new Date(instant.toEpochMilli());
    }

    public Instant getInstant() {
        return Instant.now();
    }
}
