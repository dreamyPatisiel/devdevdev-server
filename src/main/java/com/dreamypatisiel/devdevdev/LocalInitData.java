package com.dreamypatisiel.devdevdev;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.MemberNicknameDictionary;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Word;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.WordType;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.memberNicknameDictionary.MemberNicknameDictionaryRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
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
    public final static String username = "장세웅";
    public final static String userNickname = "댑댑이_User";
    public final static String userEmail = "test_user@devdevdev.com";
    public final static Role userRole = Role.ROLE_USER;
    public final static SocialType userSocialType = SocialType.KAKAO;

    public final static String adminName = "유소영";
    public final static String adminNickname = "댑댑이_Admin";
    public final static String adminEmail = "test_admin@devdevdev.com";
    public final static Role adminRole = Role.ROLE_ADMIN;
    public final static SocialType adminSocialType = SocialType.KAKAO;

    private final static int DATA_MAX_COUNT = 100;

    private final MemberRepository memberRepository;
    private final PickRepository pickRepository;
    private final PickOptionRepository pickOptionRepository;
    private final PickVoteRepository pickVoteRepository;
    private final PickOptionImageRepository pickOptionImageRepository;
    private final PickPopularScorePolicy pickPopularScorePolicy;
    private final TechArticleRepository techArticleRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ElasticTechArticleRepository elasticTechArticleRepository;
    private final CompanyRepository companyRepository;
    private final MemberNicknameDictionaryRepository memberNicknameDictionaryRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void dataInsert() {
        log.info("LocalInitData.init()");

        SocialMemberDto userSocialMemberDto = createSocialMemberDto(username, userEmail, userNickname, userSocialType,
                userRole);
        Member member = Member.createMemberBy(userSocialMemberDto);
        memberRepository.save(member);

        SocialMemberDto adminSocialMemberDto = createSocialMemberDto(adminName, adminEmail, adminNickname,
                adminSocialType, adminRole);
        memberRepository.save(Member.createMemberBy(adminSocialMemberDto));

        List<PickOption> pickOptions = createPickOptions();
        List<Pick> picks = creatPicks(pickOptions, member);
        List<PickVote> pickVotes = createPickVotes(member, picks.get(0), pickOptions);
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

        List<MemberNicknameDictionary> nicknameDictionaryWords = createNicknameDictionaryWords();
        memberNicknameDictionaryRepository.saveAll(nicknameDictionaryWords);
    }

    private List<MemberNicknameDictionary> createNicknameDictionaryWords() {
        List<MemberNicknameDictionary> nicknameDictionaryWords = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            for (WordType wordType : WordType.values()) {
                nicknameDictionaryWords.add(createMemberNicknameDictionary(wordType.getType() + i, wordType));
            }
        }
        return nicknameDictionaryWords;
    }

    private static MemberNicknameDictionary createMemberNicknameDictionary(String word, WordType wordType) {
        return MemberNicknameDictionary.builder()
                .word(new Word(word))
                .wordType(wordType)
                .build();
    }

    private static SocialMemberDto createSocialMemberDto(String username, String userEmail, String userNickname,
                                                         SocialType socialType, Role role) {
        int index = userEmail.indexOf('@');

        return SocialMemberDto.builder()
                .userId(userEmail.substring(0, index))
                .name(username)
                .email(userEmail)
                .nickname(userNickname)
                .socialType(socialType)
                .role(role)
                .build();
    }

    private List<Company> createCompanies() {
        List<Company> companies = new ArrayList<>();
        companies.add(Company.of(new CompanyName("Toss"), new Url("https://toss.tech"),
                new Url("https://toss.im/career/jobs")));
        companies.add(Company.of(new CompanyName("우아한 형제들"), new Url("https://techblog.woowahan.com"),
                new Url("https://career.woowahan.com")));
        companies.add(Company.of(new CompanyName("AWS"), new Url("https://aws.amazon.com/ko/blogs/tech"),
                new Url("https://aws.amazon.com/ko/careers")));
        companies.add(Company.of(new CompanyName("채널톡"), new Url("https://channel.io/ko/blog"),
                new Url("https://channel.io/ko/jobs")));
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
            if (creatRandomBoolean()) {
                Bookmark bookmark = Bookmark.create(member, techArticle, true);
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
        for (int number = 0; number < DATA_MAX_COUNT / 2; number++) {
            SocialMemberDto socialMemberDto = SocialMemberDto.of(userEmail + number, userSocialType.name(),
                    userRole.name(),
                    userNickname + number);
            Member member = Member.createMemberBy(socialMemberDto);
            members.add(member);
        }
        return members;
    }

    private List<PickVote> createPickVotes(Member member, Pick pick, List<PickOption> pickOptions) {
        List<PickVote> pickVotes = new ArrayList<>();
        for (int number = 0; number < DATA_MAX_COUNT / 2; number++) {
            PickVote pickVote = PickVote.createByMember(member, pick, pickOptions.get(number * 2));
            pickVotes.add(pickVote);
        }

        return pickVotes;
    }

    private List<Pick> creatPicks(List<PickOption> pickOptions, Member member) {
        String thumbnailUrl = "픽 섬네일 이미지 url";
        String author = "운영자";

        List<Pick> picks = new ArrayList<>();
        for (int number = 0; number < DATA_MAX_COUNT / 2; number++) {
            Count pickViewTotalCount = new Count(creatRandomNumber());
            Count pickCommentTotalCount = new Count(creatRandomNumber());
            Count pickVoteTotalCount = new Count(
                    pickOptions.get(number * 2).getVoteTotalCount().getCount() + pickOptions.get(number * 2 + 1)
                            .getVoteTotalCount().getCount());

            Pick pick = createPick(new Title("픽타이틀" + number), pickVoteTotalCount, pickViewTotalCount,
                    pickCommentTotalCount, thumbnailUrl + number, author,
                    List.of(pickOptions.get(number * 2), pickOptions.get(number * 2 + 1)), member);
            pick.changePopularScore(pickPopularScorePolicy);
            pick.changeEmbeddings(List.of(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9));

            picks.add(pick);
        }

        for (int number = DATA_MAX_COUNT / 2; number < DATA_MAX_COUNT; number++) {
            Count pickViewTotalCount = new Count(creatRandomNumber());
            Count pickCommentTotalCount = new Count(creatRandomNumber());
            Count pickVoteTotalCount = new Count(
                    pickOptions.get(number * 2).getVoteTotalCount().getCount() + pickOptions.get(number * 2 + 1)
                            .getVoteTotalCount().getCount());

            Pick pick = createPick(new Title("픽타이틀" + number), pickVoteTotalCount, pickViewTotalCount,
                    pickCommentTotalCount, thumbnailUrl + number, author,
                    List.of(pickOptions.get(number * 2), pickOptions.get(number * 2 + 1)), member);
            pick.changePopularScore(pickPopularScorePolicy);
            pick.changeEmbeddings(List.of(0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1));
            picks.add(pick);
        }

        return picks;
    }

    private Pick createPick(Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                            Count pickcommentTotalCount, String thumbnailUrl, String author,
                            List<PickOption> pickOptions, Member member
    ) {

        Pick pick = Pick.builder()
                .title(title)
                .member(member)
                .voteTotalCount(pickVoteTotalCount)
                .viewTotalCount(pickViewTotalCount)
                .commentTotalCount(pickcommentTotalCount)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .contentStatus(ContentStatus.APPROVAL)
                .build();

        pick.changePickOptions(pickOptions);

        return pick;
    }

    private List<PickOption> createPickOptions() {
        List<PickOption> pickOptions = new ArrayList<>();

        for (int number = 1; number <= DATA_MAX_COUNT * 2; number++) {
            List<PickOptionImage> pickOptionImages = createPickOptionImage();
            PickOption pickOption = createPickOption(new Title("픽옵션" + number), new PickOptionContents("픽콘텐츠" + number),
                    number % 2 == 1 ? PickOptionType.firstPickOption : PickOptionType.secondPickOption,
                    pickOptionImages);

            pickOption.changePickVoteCount(new Count(creatRandomNumber()));
            pickOptions.add(pickOption);

            pickOptionImageRepository.saveAll(pickOptionImages);
        }

        return pickOptions;
    }

    private List<PickOptionImage> createPickOptionImage() {
        String sampleImageUrl1 = "https://devdevdev-storage.s3.ap-northeast-2.amazonaws.com/test/pickpickpick/hexagonal-architecture.png";
        String sampleImageUrl2 = "https://devdevdev-storage.s3.ap-northeast-2.amazonaws.com/test/pickpickpick/layered-architecture.png";

        PickOptionImage pickOptionImage1 = PickOptionImage.create(
                sampleImageUrl1, "/test/pickpickpick/hexagonal-architecture.png", "hexagonal-architecture");
        PickOptionImage pickOptionImage2 = PickOptionImage.create(
                sampleImageUrl2, "/test/pickpickpick/layered-architecture.png", "layered-architecture");

        return List.of(pickOptionImage1, pickOptionImage2);
    }

    private int creatRandomNumber() {
        return (int) (Math.random() * 1_000);
    }

    private boolean creatRandomBoolean() {
        return new Random().nextBoolean();
    }

    private PickOption createPickOption(Title title, PickOptionContents pickOptionContents,
                                        PickOptionType pickOptionType, List<PickOptionImage> pickOptionImages) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePickOptionImages(pickOptionImages);

        return pickOption;
    }
}
