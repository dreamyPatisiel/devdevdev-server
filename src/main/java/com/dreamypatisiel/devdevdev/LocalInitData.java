package com.dreamypatisiel.devdevdev;

import com.dreamypatisiel.devdevdev.domain.entity.*;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.*;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.*;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@Profile(value = {"local"})
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
    private final PickPopularScorePolicy pickPopularScorePolicy;
    private final TechArticleRepository techArticleRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ElasticTechArticleRepository elasticTechArticleRepository;
    private final CompanyRepository companyRepository;

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

        List<Company> companies = createCompanies();
        List<Company> savedCompanies = companyRepository.saveAll(companies);

        Map<Long, Company> companyIdMap = getCompanyIdMap(savedCompanies);
        List<TechArticle> techArticles = createTechArticles(companyIdMap);
        techArticleRepository.saveAll(techArticles);

        List<Bookmark> bookmarks = createBookmarks(member, techArticles);
        bookmarkRepository.saveAll(bookmarks);
    }

    private List<Company> createCompanies() {
        List<Company> companies = new ArrayList<>();
        companies.add(Company.of(new CompanyName("Toss"), new Url("https://toss.tech"), new Url("https://toss.im/career/jobs")));
        companies.add(Company.of(new CompanyName("우아한 형제들"), new Url("https://techblog.woowahan.com"), new Url("https://career.woowahan.com")));
        companies.add(Company.of(new CompanyName("AWS"), new Url("https://aws.amazon.com/ko/blogs/tech"), new Url("https://aws.amazon.com/ko/careers")));
        companies.add(Company.of(new CompanyName("채널톡"), new Url("https://channel.io/ko/blog"), new Url("https://channel.io/ko/jobs")));
        return companies;
    }

    private static Map<Long, Company> getCompanyIdMap(List<Company> companies) {
        return companies.stream()
                .collect(Collectors.toMap(
                        Company::getId,
                        Function.identity()
                ));
    }

    private List<Bookmark> createBookmarks(Member member, List<TechArticle> techArticles) {
        List<Bookmark> bookmarks = new ArrayList<>();
        for (TechArticle techArticle : techArticles) {
            if(creatRandomBoolean()){
                Bookmark bookmark = Bookmark.from(member, techArticle);
                bookmarks.add(bookmark);
            }
        }
        return bookmarks;
    }

    private List<TechArticle> createTechArticles(Map<Long, Company> companyIdMap) {
        List<TechArticle> techArticles = new ArrayList<>();
        Iterable<ElasticTechArticle> elasticTechArticles = elasticTechArticleRepository.findAll();
        for (ElasticTechArticle elasticTechArticle : elasticTechArticles) {
            Company company = companyIdMap.get(elasticTechArticle.getCompanyId());
            TechArticle techArticle = TechArticle.of(elasticTechArticle, company);
            techArticles.add(techArticle);
        }
        return techArticles;
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

            Pick pick = createPick(new Title("픽타이틀"+number), pickVoteTotalCount, pickViewTotalCount,
                    pickCommentTotalCount, thumbnailUrl+number, author,
                    List.of(pickOptions.get(number*2), pickOptions.get(number*2+1)), List.of(pickVotes.get(number)));
            pick.changePopularScore(pickPopularScorePolicy);
            picks.add(pick);
        }

        for(int number = DATA_MAX_COUNT / 2; number < DATA_MAX_COUNT; number++) {
            Count pickViewTotalCount = new Count(creatRandomNumber());
            Count pickCommentTotalCount = new Count(creatRandomNumber());
            Count pickVoteTotalCount = new Count(pickOptions.get(number*2).getVoteTotalCount().getCount() + pickOptions.get(number*2+1).getVoteTotalCount().getCount());

            Pick pick = createPick(new Title("픽타이틀"+number), pickVoteTotalCount, pickViewTotalCount,
                    pickCommentTotalCount, thumbnailUrl+number, author,
                    List.of(pickOptions.get(number*2), pickOptions.get(number*2+1)), List.of());
            pick.changePopularScore(pickPopularScorePolicy);
            picks.add(pick);
        }

        return picks;
    }

    private Pick createPick(Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                            Count pickcommentTotalCount, String thumbnailUrl, String author,
                            List<PickOption> pickOptions, List<PickVote> pickVotes
    ) {

        Pick pick = Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteTotalCount)
                .viewTotalCount(pickViewTotalCount)
                .commentTotalCount(pickcommentTotalCount)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .build();

        pick.changePickOptions(pickOptions);
        pick.changePickVote(pickVotes);

        return pick;
    }

    private List<PickOption> createPickOptions() {
        List<PickOption> pickOptions = new ArrayList<>();
        List<PickOptionImage> pickOptionImages = createPickOptionImage();
        for(int number = 1; number <= DATA_MAX_COUNT*2; number++) {
            PickOption pickOption = createPickOption(new Title("픽옵션"+number), new PickOptionContents("픽콘텐츠"+number));
            pickOption.changePickVoteCount(new Count(creatRandomNumber()));
            pickOptions.add(pickOption);
        }

        return pickOptions;
    }

    private List<PickOptionImage> createPickOptionImage() {
        String sampleImageUrl1 = "https://devdevdev-storage.s3.ap-northeast-2.amazonaws.com/test/pickpickpick/hexagonal-architecture.png";
        String sampleImageUrl2 = "https://devdevdev-storage.s3.ap-northeast-2.amazonaws.com/test/pickpickpick/layered-architecture.png";

        PickOptionImage pickOptionImage1 = PickOptionImage.create(
                sampleImageUrl1, "/test/pickpickpick/hexagonal-architecture.png", "firstPickOptionImage");
        PickOptionImage pickOptionImage2 = PickOptionImage.create(
                sampleImageUrl2, "/test/pickpickpick/layered-architecture.png", "firstPickOptionImage");

        return List.of(pickOptionImage1, pickOptionImage2);
    }

    private int creatRandomNumber() {
        return (int) (Math.random() * 1_000);
    }

    private boolean creatRandomBoolean() {
        return new Random().nextBoolean();
    }

    private PickOption createPickOption(Title title, PickOptionContents pickOptionContents) {
        return PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .build();
    }
}
