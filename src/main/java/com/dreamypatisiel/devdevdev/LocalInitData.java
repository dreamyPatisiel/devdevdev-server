package com.dreamypatisiel.devdevdev;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@Profile(value = {"local", "dev"})
@RequiredArgsConstructor
@Transactional
public class LocalInitData {
    public final static String userNickname = "댑댑이_User";
    public final static String userEmail = "test_user@devdevdev.com";
    public final static Role userRole = Role.ROLE_USER;
    public final static SocialType userSocialType = SocialType.KAKAO;

    public final static String adminNickname = "댑댑이_Admin";
    public final static String adminEmail = "test_admin@devdevdev.com";
    public final static Role adminRole = Role.ROLE_ADMIN;
    public final static SocialType adminSocialType = SocialType.KAKAO;

    private final static int DATA_MAX_COUNT = 100;

    private final MemberRepository memberRepository;
    private final PickRepository pickRepository;
    private final PickOptionRepository pickOptionRepository;
    private final PickVoteRepository pickVoteRepository;


    @EventListener(ApplicationReadyEvent.class)
    public void dataInsert() {
        log.info("LocalInitData.init()");

        SocialMemberDto userSocialMemberDto = SocialMemberDto.of(userEmail, userSocialType.name(), userRole.name(), userNickname);
        Member member = Member.createMemberBy(userSocialMemberDto);
        memberRepository.save(member);

        SocialMemberDto adminSocialMemberDto = SocialMemberDto.of(adminEmail, adminSocialType.name(), adminRole.name(), adminNickname);
        memberRepository.save(Member.createMemberBy(adminSocialMemberDto));

        List<PickOption> pickOptions = createPickOptions();
        List<PickVote> pickVotes = createPickVotes(member, pickOptions);
        List<Pick> picks = creatPicks(pickOptions, pickVotes);
        pickRepository.saveAll(picks);
        pickVoteRepository.saveAll(pickVotes);
        pickOptionRepository.saveAll(pickOptions);
    }

    private List<Member> createMembers() {
        List<Member> members = new ArrayList<>();
        for(int number = 0; number < DATA_MAX_COUNT / 2; number++) {
            SocialMemberDto socialMemberDto = SocialMemberDto.of(userEmail+number, userSocialType.name(), userRole.name(),
                    userNickname+number);
            Member member = Member.createMemberBy(socialMemberDto);
            members.add(member);
        }
        return members;
    }

    private List<PickVote> createPickVotes(Member member, List<PickOption> pickOptions) {
        List<PickVote> pickVotes = new ArrayList<>();
        for(int number = 0; number < DATA_MAX_COUNT / 2; number++) {
            PickVote pickVote = PickVote.create(member, pickOptions.get(number*2));
            pickVotes.add(pickVote);
        }

        return pickVotes;
    }

    private List<Pick> creatPicks(List<PickOption> pickOptions, List<PickVote> pickVotes) {
        String thumbnailUrl = "픽 섬네일 이미지 url";
        String author = "운영자";

        List<Pick> picks = new ArrayList<>();
        for(int number = 0; number < DATA_MAX_COUNT / 2; number++) {
            Count pickViewTotalCount = new Count(creatRandomNumber());
            Count pickCommentTotalCount = new Count(creatRandomNumber());
            Count pickVoteTotalCount = new Count(pickOptions.get(number*2).getVoteTotalCount().getCount() + pickOptions.get(number*2+1).getVoteTotalCount().getCount());

            Pick pick = Pick.create(new Title("픽타이틀"+number), pickVoteTotalCount, pickViewTotalCount,
                    pickCommentTotalCount, thumbnailUrl+number, author,
                    List.of(pickOptions.get(number*2), pickOptions.get(number*2+1)), List.of(pickVotes.get(number)));
            picks.add(pick);
        }

        for(int number = DATA_MAX_COUNT / 2; number < DATA_MAX_COUNT; number++) {
            Count pickViewTotalCount = new Count(creatRandomNumber());
            Count pickCommentTotalCount = new Count(creatRandomNumber());
            Count pickVoteTotalCount = new Count(pickOptions.get(number*2).getVoteTotalCount().getCount() + pickOptions.get(number*2+1).getVoteTotalCount().getCount());

            Pick pick = Pick.create(new Title("픽타이틀"+number), pickVoteTotalCount, pickViewTotalCount,
                    pickCommentTotalCount, thumbnailUrl+number, author,
                    List.of(pickOptions.get(number*2), pickOptions.get(number*2+1)), List.of());
            picks.add(pick);
        }

        return picks;
    }

    private List<PickOption> createPickOptions() {
        List<PickOption> pickOptions = new ArrayList<>();
        for(int number = 1; number <= DATA_MAX_COUNT*2; number++) {
            PickOption pickOption = PickOption.create(new Title("픽옵션"+number), new PickContents("픽콘텐츠"+number), new Count(creatRandomNumber()));
            pickOptions.add(pickOption);
        }

        return pickOptions;
    }

    private int creatRandomNumber() {
        return (int) (Math.random() * 1_000);
    }
}
