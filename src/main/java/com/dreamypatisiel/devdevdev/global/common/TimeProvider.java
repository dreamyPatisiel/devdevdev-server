package com.dreamypatisiel.devdevdev.global.common;


import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

public class TimeProvider {

    public LocalDateTime getLocalDateTimeNow() {
        return LocalDateTime.now();
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
