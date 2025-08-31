package com.dreamypatisiel.devdevdev.domain.policy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NicknameChangePolicy {
    @Value("${nickname.change.interval.minutes:1440}")
    private int nicknameChangeIntervalMinutes;

    public int getNicknameChangeIntervalMinutes() {
        return nicknameChangeIntervalMinutes;
    }
}
