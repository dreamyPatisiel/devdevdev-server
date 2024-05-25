package com.dreamypatisiel.devdevdev.domain.service.member;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.MyPickMainOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.MyPickMainResponse;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberProvider memberProvider;
    private final MemberRepository memberRepository;
    private final PickRepository pickRepository;
    private final TimeProvider timeProvider;

    /**
     * 회원 탈퇴 회원의 북마크와 회원 정보를 삭제합니다.
     */
    @Transactional
    public void deleteMember(Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 회원 삭제
        member.deleteMember(timeProvider.getLocalDateTimeNow());
    }

    /**
     * 회원 자신이 작성한 픽픽픽을 조회한다.
     */
    public Slice<MyPickMainResponse> findMyPickMain(Pageable pageable, Long pickId, Authentication authentication) {

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 회원이 작성한 픽픽픽 조회
        Slice<Pick> findPicks = pickRepository.findPicksByMemberAndCursor(pageable, findMember, pickId);

        // 데이터 가공
        List<MyPickMainResponse> myPickMainsResponse = findPicks.stream()
                .map(pick -> MyPickMainResponse.of(pick, mapToMyPickMainOption(pick)))
                .toList();

        return new SliceImpl<>(myPickMainsResponse, pageable, findPicks.hasNext());
    }

    private List<MyPickMainOptionResponse> mapToMyPickMainOption(Pick pick) {
        return pick.getPickOptions().stream()
                .map(pickOption -> MyPickMainOptionResponse.of(pick, pickOption))
                .toList();
    }
}