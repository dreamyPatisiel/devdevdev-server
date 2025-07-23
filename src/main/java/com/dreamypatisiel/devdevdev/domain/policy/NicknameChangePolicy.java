package com.dreamypatisiel.devdevdev.domain.policy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NicknameChangePolicy {
    @Value("${nickname.change.interval.hours:24}")
    private int nicknameChangeIntervalHours;

    public int getNicknameChangeIntervalHours() {
        return nicknameChangeIntervalHours;
    }
}
