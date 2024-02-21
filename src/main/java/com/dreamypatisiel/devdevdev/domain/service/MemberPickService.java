package com.dreamypatisiel.devdevdev.domain.service;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.PickOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PicksResponse;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
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
public class MemberPickService implements PickService {

    private final PickRepository pickRepository;
    private final MemberRepository memberRepository;

    @Override
    public Slice<PicksResponse> findPicksMain(Pageable pageable, Long pickId, Authentication authentication) {
        // 픽픽픽 조회
        Slice<Pick> picks = pickRepository.findPicksByLtPickId(pageable, pickId);

        // 회원 조회
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String email = userPrincipal.getEmail();
        SocialType socialType = userPrincipal.getSocialType();
        Member member = memberRepository.findMemberByEmailAndSocialType(new Email(email), socialType)
                .orElseThrow(() -> new MemberException(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE));

        // 데이터 가공
        List<PicksResponse> picksResponses = picks.stream()
                .map(pick -> mapToPickResponse(pick, picks, member))
                .toList();

        return new SliceImpl<>(picksResponses, pageable, picks.hasNext());
    }

    private PicksResponse mapToPickResponse(Pick pick, Slice<Pick> picks, Member member) {
        return PicksResponse.builder()
                .id(pick.getId())
                .title(pick.getTitle())
                .voteTotalCount(pick.getVoteTotalCount())
                .commentTotalCount(pick.getCommentTotalCount())
                .isVoted(isVotedByPickAndMember(pick, member))
                .pickOptions(mapToPickOptionsResponse(pick, member))
                .build();
    }

    private boolean isVotedByPickAndMember(Pick pick, Member member) {
        return pick.getPickVotes().stream()
                .filter(pickVote -> pickVote.getPick().isEqualsPick(pick))
                .anyMatch(pickVote -> pickVote.getMember().isEqualsMember(member));
    }

    private List<PickOptionResponse> mapToPickOptionsResponse(Pick pick, Member member) {
        return pick.getPickOptions().stream()
                .map(pickOption -> mapToPickOptionResponse(pick, pickOption, member))
                .toList();
    }

    private PickOptionResponse mapToPickOptionResponse(Pick pick, PickOption pickOption, Member member) {
        return PickOptionResponse.builder()
                .id(pickOption.getId())
                .title(pickOption.getTitle())
                .percent(PickOption.calculatePercentBy(pick, pickOption))
                .isPicked(isPickedPickOptionByMember(pick, pickOption, member))
                .build();
    }

    private Boolean isPickedPickOptionByMember(Pick pick, PickOption pickOption, Member member) {
        return pick.getPickVotes().stream()
                .filter(pickVote -> pickVote.getPickOption().equals(pickOption))
                .anyMatch(pickVote -> pickVote.getMember().equals(member));
    }
}
