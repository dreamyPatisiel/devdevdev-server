package com.dreamypatisiel.devdevdev.domain.repository;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {
    private final Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    public SseEmitter save(Member member, SseEmitter sseEmitter) {
        return sseEmitters.put(member.getId(), sseEmitter);
    }

    public void remove(Member member) {
        sseEmitters.remove(member.getId());
    }

    public SseEmitter findByMemberId(Member member) {
        return sseEmitters.get(member.getId());
    }

    public Collection<SseEmitter> findAll() {
        return sseEmitters.values();
    }
}
