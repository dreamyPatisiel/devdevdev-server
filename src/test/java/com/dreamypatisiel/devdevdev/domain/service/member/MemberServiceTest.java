package com.dreamypatisiel.devdevdev.domain.service.member;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.MEMBER_INCOMPLETE_SURVEY_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyAnswer;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyQuestion;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyQuestionOption;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyVersion;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyVersionQuestionMapper;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CustomSurveyAnswer;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyAnswerRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyQuestionOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyQuestionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyVersionQuestionMapperRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyVersionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.web.dto.response.member.MemberExitSurveyQuestionOptionResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.member.MemberExitSurveyQuestionResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.member.MemberExitSurveyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.MyPickMainResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticsearchSupportTest;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.SurveyException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.web.dto.request.member.RecordMemberExitSurveyAnswerRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.member.RecordMemberExitSurveyQuestionOptionsRequest;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

@SpringBootTest
class MemberServiceTest extends ElasticsearchSupportTest {

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

    @Autowired
    EntityManager em;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    BookmarkRepository bookmarkRepository;
    @Autowired
    PickRepository pickRepository;
    @Autowired
    MemberService memberService;
    @Autowired
    MemberProvider memberProvider;
    @Autowired
    PickOptionRepository pickOptionRepository;
    @Autowired
    PickVoteRepository pickVoteRepository;
    @Autowired
    SurveyVersionRepository surveyVersionRepository;
    @Autowired
    SurveyVersionQuestionMapperRepository surveyVersionQuestionMapperRepository;
    @Autowired
    SurveyQuestionRepository surveyQuestionRepository;
    @Autowired
    SurveyQuestionOptionRepository surveyQuestionOptionRepository;
    @Autowired
    SurveyAnswerRepository surveyAnswerRepository;

    @Test
    @DisplayName("회원이 회원탈퇴 설문조사를 완료하지 않으면 탈퇴가 불가능하다.")
    void deleteMemberIncompleteSurvey() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 서베이 버전 생성
        SurveyVersion surveyVersion1 = SurveyVersion.builder()
                .versionName("회원탈퇴-A")
                .isActive(true)
                .build();
        SurveyVersion surveyVersion2 = SurveyVersion.builder()
                .versionName("회원탈퇴-B")
                .isActive(false)
                .build();
        surveyVersionRepository.saveAll(List.of(surveyVersion1, surveyVersion2));

        // 서베이 질문 생성
        SurveyQuestion question = SurveyQuestion.builder()
                .title("#nickName님 회원 탈퇴하는 이유를 알려주세요.")
                .content("회원 탈퇴하는 이유를 상세하게 알려주세요.")
                .sortOrder(0)
                .build();
        surveyQuestionRepository.save(question);

        // 서베이 매퍼 생성
        SurveyVersionQuestionMapper mapper = SurveyVersionQuestionMapper.builder()
                .surveyVersion(surveyVersion1)
                .surveyQuestion(question)
                .build();
        surveyVersionQuestionMapperRepository.save(mapper);

        // 서베이 질문 옵션 생성
        SurveyQuestionOption option1 = SurveyQuestionOption.builder()
                .title("채용정보가 부족해요.")
                .sortOrder(0)
                .build();
        option1.changeSurveyQuestion(question);

        SurveyQuestionOption option2 = SurveyQuestionOption.builder()
                .title("정보가 부족해요.")
                .sortOrder(1)
                .build();
        option2.changeSurveyQuestion(question);

        SurveyQuestionOption option3 = SurveyQuestionOption.builder()
                .title("기타.")
                .sortOrder(2)
                .build();
        option3.changeSurveyQuestion(question);
        surveyQuestionOptionRepository.saveAll(List.of(option1, option2, option3));

        // when // then
        assertThatThrownBy(() -> memberService.deleteMember(authentication))
                .isInstanceOf(SurveyException.class)
                .hasMessage(MEMBER_INCOMPLETE_SURVEY_MESSAGE);

    }

    @Test
    @DisplayName("탈퇴 설문조사를 완료한 회원이 회원탈퇴를 요청하면 "
            + "회원의 is_deleted가 true로 변경되고 "
            + "memberProvider에서 더이상 조회되지 않는다.")
    void deleteMemberTest() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        Member savedMember = memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 서베이 버전 생성
        SurveyVersion surveyVersion1 = SurveyVersion.builder()
                .versionName("회원탈퇴-A")
                .isActive(true)
                .build();
        SurveyVersion surveyVersion2 = SurveyVersion.builder()
                .versionName("회원탈퇴-B")
                .isActive(false)
                .build();
        surveyVersionRepository.saveAll(List.of(surveyVersion1, surveyVersion2));

        // 서베이 질문 생성
        SurveyQuestion question = SurveyQuestion.builder()
                .title("#nickName님 회원 탈퇴하는 이유를 알려주세요.")
                .content("회원 탈퇴하는 이유를 상세하게 알려주세요.")
                .sortOrder(0)
                .build();
        surveyQuestionRepository.save(question);

        // 서베이 매퍼 생성
        SurveyVersionQuestionMapper mapper = SurveyVersionQuestionMapper.builder()
                .surveyVersion(surveyVersion1)
                .surveyQuestion(question)
                .build();
        surveyVersionQuestionMapperRepository.save(mapper);

        // 서베이 질문 옵션 생성
        SurveyQuestionOption option1 = SurveyQuestionOption.builder()
                .title("채용정보가 부족해요.")
                .sortOrder(0)
                .build();
        option1.changeSurveyQuestion(question);

        SurveyQuestionOption option2 = SurveyQuestionOption.builder()
                .title("정보가 부족해요.")
                .sortOrder(1)
                .build();
        option2.changeSurveyQuestion(question);

        SurveyQuestionOption option3 = SurveyQuestionOption.builder()
                .title("기타.")
                .sortOrder(2)
                .build();
        option3.changeSurveyQuestion(question);
        surveyQuestionOptionRepository.saveAll(List.of(option1, option2, option3));

        SurveyAnswer surveyAnswer = SurveyAnswer.builder()
                .surveyQuestion(question)
                .surveyQuestionOption(option1)
                .member(savedMember)
                .build();
        surveyAnswerRepository.save(surveyAnswer);

        // when
        memberService.deleteMember(authentication);

        // then
        assertThat(savedMember)
                .isNotNull()
                .extracting(Member::getIsDeleted)
                .isEqualTo(true);

        assertThatThrownBy(() -> memberProvider.getMemberByAuthentication(authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("회원이 자신이 작성한 픽픽픽 목록을 작성시간 내림차순으로 무한스크롤 방식으로 조회한다.")
    void findMyPickMain() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        pickSetup(member, ContentStatus.APPROVAL, new Title("쏘영이의 주류 픽픽픽!"),
                new Title("쏘주가 최고다!"), new PickOptionContents("참이슬을 못참지!"),
                new Title("와인이 최고다!"), new PickOptionContents("레드와인은 못참지!"));
        pickSetup(member, ContentStatus.READY, new Title("쏘영이의 치킨 픽픽픽!"),
                new Title("후라이드 치킨이 최고다!"), new PickOptionContents("후라이드는 못참지!"),
                new Title("양념 치킨이 최고다!"), new PickOptionContents("양념은 못참지!"));
        pickSetup(member, ContentStatus.REJECT, new Title("쏘영이의 가수 픽픽픽!"),
                new Title("장범준이 최고다!"), new PickOptionContents("봄바람 휘날리며~"),
                new Title("뉴진스가 곧 세계다"), new PickOptionContents("하입보이~!"));
        Long pickId = pickSetup(member, ContentStatus.REJECT, new Title("쏘영이의 자동차 픽픽픽!"),
                new Title("벤츠가 최고다!"), new PickOptionContents("지바겐이 제일 좋아!"),
                new Title("아우디가 최고다!"), new PickOptionContents("벤츠 너무 무서워!"));

        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 3);

        // when
        Slice<MyPickMainResponse> response = memberService.findMyPickMain(pageable, pickId, authentication);

        // then
        List<MyPickMainResponse> myPickMainsResponse = response.getContent();
        assertThat(myPickMainsResponse).hasSize(3)
                .extracting("title", "isVoted", "contentStatus")
                .containsExactly(
                        tuple(new Title("쏘영이의 가수 픽픽픽!").getTitle(), true, ContentStatus.REJECT.name()),
                        tuple(new Title("쏘영이의 치킨 픽픽픽!").getTitle(), true, ContentStatus.READY.name()),
                        tuple(new Title("쏘영이의 주류 픽픽픽!").getTitle(), true, ContentStatus.APPROVAL.name())
                );
    }

    @Test
    @DisplayName("활성화 상태이고 가장 최근에 작성된 서베이 버전의 서베이 질문과 질문 옵션을 조회한다.")
    void findMemberExitSurvey() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 서베이 버전 생성
        SurveyVersion surveyVersion1 = SurveyVersion.builder()
                .versionName("회원탈퇴-A")
                .isActive(true)
                .build();
        SurveyVersion surveyVersion2 = SurveyVersion.builder()
                .versionName("회원탈퇴-B")
                .isActive(false)
                .build();
        surveyVersionRepository.saveAll(List.of(surveyVersion1, surveyVersion2));

        // 서베이 질문 생성
        SurveyQuestion question = SurveyQuestion.builder()
                .title("#nickName님 회원 탈퇴하는 이유를 알려주세요.")
                .content("회원 탈퇴하는 이유를 상세하게 알려주세요.")
                .sortOrder(0)
                .build();
        surveyQuestionRepository.save(question);

        // 서베이 매퍼 생성
        SurveyVersionQuestionMapper mapper = SurveyVersionQuestionMapper.builder()
                .surveyVersion(surveyVersion1)
                .surveyQuestion(question)
                .build();
        surveyVersionQuestionMapperRepository.save(mapper);

        // 서베이 질문 옵션 생성
        SurveyQuestionOption option1 = SurveyQuestionOption.builder()
                .title("채용정보가 부족해요.")
                .sortOrder(0)
                .build();
        option1.changeSurveyQuestion(question);

        SurveyQuestionOption option2 = SurveyQuestionOption.builder()
                .title("정보가 부족해요.")
                .sortOrder(1)
                .build();
        option2.changeSurveyQuestion(question);

        SurveyQuestionOption option3 = SurveyQuestionOption.builder()
                .title("기타.")
                .sortOrder(2)
                .build();
        option3.changeSurveyQuestion(question);
        surveyQuestionOptionRepository.saveAll(List.of(option1, option2, option3));

        // when
        MemberExitSurveyResponse response = memberService.findMemberExitSurvey(authentication);

        // then
        assertThat(response.getSurveyVersionId()).isEqualTo(surveyVersion1.getId());

        List<MemberExitSurveyQuestionResponse> surveyQuestions = response.getSurveyQuestions();
        assertThat(surveyQuestions).hasSize(1)
                .extracting("id", "content", "sortOrder")
                .containsExactly(
                        tuple(question.getId(), question.getContent(), question.getSortOrder())
                );

        List<MemberExitSurveyQuestionOptionResponse> surveyQuestionOptions = surveyQuestions.get(0)
                .getSurveyQuestionOptions();
        assertThat(surveyQuestionOptions).hasSize(3)
                .extracting("id", "title", "content", "sortOrder")
                .containsExactly(
                        tuple(option1.getId(), option1.getTitle(), option1.getContent(), option1.getSortOrder()),
                        tuple(option2.getId(), option2.getTitle(), option2.getContent(), option2.getSortOrder()),
                        tuple(option3.getId(), option3.getTitle(), option3.getContent(), option3.getSortOrder())
                );
    }

    @Test
    @DisplayName("회원이 커서 방식으로 기술블로그 북마크 목록을 조회하여 응답을 생성한다.")
    void getBookmarkedTechArticles() {
        // given
        Pageable pageable = PageRequest.of(0, 1);

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Bookmark bookmark = createBookmark(member, firstTechArticle, true);
        bookmarkRepository.save(bookmark);

        em.flush();
        em.clear();

        // when
        Slice<TechArticleMainResponse> findTechArticles = memberService.getBookmarkedTechArticles(pageable,
                null, null, authentication);

        // then
        assertThat(findTechArticles)
                .hasSize(pageable.getPageSize())
                .extracting(TechArticleMainResponse::getIsBookmarked)
                .contains(true);
    }

    @Test
    @DisplayName("커서 방식으로 기술블로그 북마크 목록을 조회할 때 회원이 없으면 예외가 발생한다.")
    void getBookmarkedTechArticlesNotFoundMemberException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(
                () -> memberService.getBookmarkedTechArticles(pageable, null, null, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("회원탈퇴 서베이 이력을 기록한다.")
    void recordMemberExitSurveyAnswer() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 서베이 질문 생성
        SurveyQuestion question = SurveyQuestion.builder()
                .title("#nickName님 회원 탈퇴하는 이유를 알려주세요.")
                .content("회원 탈퇴하는 이유를 상세하게 알려주세요.")
                .sortOrder(0)
                .build();
        surveyQuestionRepository.save(question);

        // 서베이 질문 옵션 생성
        SurveyQuestionOption option1 = SurveyQuestionOption.builder()
                .title("채용정보가 부족해요.")
                .sortOrder(0)
                .build();
        option1.changeSurveyQuestion(question);

        SurveyQuestionOption option2 = SurveyQuestionOption.builder()
                .title("기타")
                .content("10자 이상 입력해주세요.")
                .sortOrder(0)
                .build();
        option2.changeSurveyQuestion(question);
        surveyQuestionOptionRepository.saveAll(List.of(option1, option2));

        RecordMemberExitSurveyQuestionOptionsRequest memberExitSurveyQuestionOptions1 = RecordMemberExitSurveyQuestionOptionsRequest.builder()
                .id(option1.getId())
                .build();

        RecordMemberExitSurveyQuestionOptionsRequest memberExitSurveyQuestionOptions2 = RecordMemberExitSurveyQuestionOptionsRequest.builder()
                .id(option2.getId())
                .message("i think so.. this service is...")
                .build();

        RecordMemberExitSurveyAnswerRequest request = RecordMemberExitSurveyAnswerRequest.builder()
                .questionId(question.getId())
                .memberExitSurveyQuestionOptions(
                        List.of(memberExitSurveyQuestionOptions1, memberExitSurveyQuestionOptions2))
                .build();

        // when
        memberService.recordMemberExitSurveyAnswer(request, authentication);

        // then
        List<SurveyAnswer> surveyAnswers = surveyAnswerRepository.findAllByMember(member);
        assertThat(surveyAnswers).hasSize(2)
                .extracting("customMessage", "surveyQuestion", "surveyQuestionOption")
                .containsExactly(
                        tuple(null, question, option1),
                        tuple(new CustomSurveyAnswer("i think so.. this service is..."), question, option2)
                );
    }

    @Test
    @DisplayName("회원탈퇴 서베이 이력을 저장할 때 회원이 없으면 예외가 발생한다.")
    void recordMemberExitSurveyAnswerMemberException() {
        // given
        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(
                () -> memberService.recordMemberExitSurveyAnswer(null, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    private Long pickSetup(Member member, ContentStatus contentStatus, Title pickTitle, Title firstPickOptionTitle,
                           PickOptionContents firstPickOptionContents, Title secondPickOptinTitle,
                           PickOptionContents secondPickOptionContents) {
        // 픽픽픽 생성
        Count voteTotalCount = new Count(1);
        Pick pick = createPick(member, pickTitle, voteTotalCount, new Count(2), new Count(3), contentStatus);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, PickOptionType.firstPickOption, firstPickOptionTitle,
                firstPickOptionContents, voteTotalCount);
        PickOption secondPickOption = createPickOption(pick, PickOptionType.secondPickOption, secondPickOptinTitle,
                secondPickOptionContents, new Count(0));
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(pick, firstPickOption, member);
        pickVoteRepository.save(pickVote);

        return pick.getId();
    }

    private PickVote createPickVote(Pick pick, PickOption firstPickOption, Member member) {
        return PickVote.builder()
                .pick(pick)
                .pickOption(firstPickOption)
                .member(member)
                .build();
    }

    private PickOption createPickOption(Pick pick, PickOptionType pickOptionType, Title title,
                                        PickOptionContents contents, Count voteTotalCount) {
        PickOption pickOption = PickOption.builder()
                .pickOptionType(pickOptionType)
                .title(title)
                .contents(contents)
                .voteTotalCount(voteTotalCount)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private Pick createPick(Member member, Title title, Count voteTotalCount, Count viewTotalCount,
                            Count commentTotalCount, ContentStatus contentStatus) {
        return Pick.builder()
                .member(member)
                .title(title)
                .voteTotalCount(voteTotalCount)
                .viewTotalCount(viewTotalCount)
                .commentTotalCount(commentTotalCount)
                .contentStatus(contentStatus)
                .build();
    }

    private SocialMemberDto createSocialDto(String userId, String name, String nickName, String password, String email,
                                            String socialType, String role) {
        return SocialMemberDto.builder()
                .userId(userId)
                .name(name)
                .nickname(nickName)
                .password(password)
                .email(email)
                .socialType(SocialType.valueOf(socialType))
                .role(Role.valueOf(role))
                .build();
    }

    private Bookmark createBookmark(Member member, TechArticle techArticle, boolean status) {
        return Bookmark.builder()
                .member(member)
                .techArticle(techArticle)
                .status(status)
                .build();
    }
}