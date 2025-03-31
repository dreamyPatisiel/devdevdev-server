package com.dreamypatisiel.devdevdev.domain.repository;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {
    private final Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    public SseEmitter save(Member member, SseEmitter emitter) {
        return sseEmitters.put(member.getId(), emitter);
    }

    public void remove(Long memberId) {
        sseEmitters.remove(memberId);
    }

    public void remove(Member member) {
        sseEmitters.remove(member.getId());
    }

    public SseEmitter findByMemberId(Member member) {
        return sseEmitters.get(member.getId());
    }

    public ConcurrentHashMap<Long, SseEmitter> findByMemberIn(Set<Member> members) {
        // 멤버 집합에서 ID들을 추출합니다.
        Set<Long> memberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toSet());

        // 기존 sseEmitters 맵에서 멤버 ID에 해당하는 SSE Emitter만 필터링하여 새 ConcurrentHashMap에 담습니다.
        return sseEmitters.entrySet().stream()
                .filter(entry -> memberIds.contains(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        ConcurrentHashMap::new
                ));

    }

    public Set<SseEmitter> findAll() {
        return new HashSet<>(sseEmitters.values());
    }
}
