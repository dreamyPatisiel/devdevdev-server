package com.dreamypatisiel.devdevdev;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import java.util.ArrayList;
import java.util.List;
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

    @EventListener(ApplicationReadyEvent.class)
    public void dataInsert() {
        log.info("LocalInitData.init()");

        SocialMemberDto userSocialMemberDto = SocialMemberDto.of(userEmail, userSocialType.name(), userRole.name(), userNickname);
        memberRepository.save(Member.createMemberBy(userSocialMemberDto));

        SocialMemberDto adminSocialMemberDto = SocialMemberDto.of(adminEmail, adminSocialType.name(), adminRole.name(), adminNickname);
        memberRepository.save(Member.createMemberBy(adminSocialMemberDto));

        List<PickOption> pickOptions = createPickOptions();
        List<Pick> picks = creatPicks(pickOptions);
        pickRepository.saveAll(picks);
        pickOptionRepository.saveAll(pickOptions);
    }

    private List<Pick> creatPicks(List<PickOption> pickOptions) {
        Count pickViewTotalCount = new Count(1);
        Count pickCommentTotalCount = new Count(0);
        String thumbnailUrl = "픽 섬네일 이미지 url";
        String author = "운영자";

        List<Pick> picks = new ArrayList<>();
        for(int number = 0; number < DATA_MAX_COUNT; number++) {
            Count pickVoteTotalCount = new Count(pickOptions.get(number*2).getVoteTotalCount().getCount() + pickOptions.get(number*2+1).getVoteTotalCount().getCount());
            Pick pick = Pick.create(new Title("픽타이틀"+number), pickVoteTotalCount, pickViewTotalCount,
                    pickCommentTotalCount, thumbnailUrl+number, author, List.of(pickOptions.get(number*2), pickOptions.get(number*2+1)), List.of());
            picks.add(pick);
        }

        return picks;
    }

    private List<PickOption> createPickOptions() {
        List<PickOption> pickOptions = new ArrayList<>();
        for(int number = 1; number <= DATA_MAX_COUNT*2; number++) {
            PickOption pickOption = PickOption.create(new Title("픽옵션"+number), new PickContents("픽콘텐츠"+number), new Count(number));
            pickOptions.add(pickOption);
        }

        return pickOptions;
    }
}
