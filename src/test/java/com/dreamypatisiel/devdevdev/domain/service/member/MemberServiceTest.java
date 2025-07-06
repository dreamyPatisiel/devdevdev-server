package com.dreamypatisiel.devdevdev.domain.service.member;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.MEMBER_INCOMPLETE_SURVEY_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.entity.*;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.*;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.exception.NicknameExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyAnswerRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyQuestionOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyQuestionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyVersionQuestionMapperRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyVersionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.SubscriptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticsearchSupportTest;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.NicknameException;
import com.dreamypatisiel.devdevdev.exception.SurveyException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.comment.MyWrittenCommentFilter;
import com.dreamypatisiel.devdevdev.web.dto.request.comment.MyWrittenCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.member.RecordMemberExitSurveyAnswerRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.member.RecordMemberExitSurveyQuestionOptionsRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.comment.MyWrittenCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.member.MemberExitSurveyQuestionOptionResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.member.MemberExitSurveyQuestionResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.member.MemberExitSurveyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.MyPickMainResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.subscription.SubscribedCompanyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;
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
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    TechCommentRepository techCommentRepository;
    @Autowired
    PickCommentRepository pickCommentRepository;
    @Autowired
    SubscriptionRepository subscriptionRepository;

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
    @DisplayName("회원이 커서 방식으로 기술블로그 북마크 목록을 조회할 때 회원이 없으면 예외가 발생한다.")
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

    @Test
    @DisplayName("회원이 작성한 픽픽픽, 기술블로그 댓글을 작성시간 내림차순으로 무한스크롤 방식으로 조회한다.")
    void findMyWrittenComments_ALL() {
        // given
        DateTimeProvider dateTimeProvider = mock(DateTimeProvider.class);
        AuditingHandler auditingHandler = mock(AuditingHandler.class);
        auditingHandler.setDateTimeProvider(dateTimeProvider);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 1, 1, 0, 0)));

        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 픽픽픽 생성
        Pick pick = createPick("픽픽픽", member);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption pickOption = createPickOption(pick, "픽픽픽 A", PickOptionType.firstPickOption);
        pickOptionRepository.save(pickOption);

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(pick, pickOption, member);
        pickVoteRepository.save(pickVote);

        // 픽픽픽 댓글 생성
        PickComment pickComment1 = createPickComment(pick, member, pickVote, null, null, "픽픽픽 댓글1", true, 0L);
        PickComment pickComment2 = createPickComment(pick, member, null, pickComment1, pickComment1, "픽픽픽 댓글2", false,
                1L);
        PickComment pickComment3 = createPickComment(pick, member, null, pickComment1, pickComment2, "픽픽픽 댓글3", false,
                2L);
        PickComment pickComment4 = createPickComment(pick, member, null, pickComment1, pickComment3, "픽픽픽 댓글4", false,
                3L);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 1, 1, 0, 0, 0, 0)));
        pickCommentRepository.save(pickComment1);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 2, 1, 0, 0, 0, 0)));
        pickCommentRepository.save(pickComment2);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 3, 1, 0, 0, 0, 0)));
        pickCommentRepository.save(pickComment3);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 4, 1, 0, 0, 0, 0)));
        pickCommentRepository.save(pickComment4);

        // 기술블로그 회사 생성
        Company company = createCompany("DreamyPatisiel");
        companyRepository.save(company);

        // 기술블로그 생성
        TechArticle techArticle = createTechArticle(company, "기술블로그 제목");
        techArticleRepository.save(techArticle);

        // 기술블로그 댓글 생성
        TechComment techComment1 = createTechComment(techArticle, member, null, null, "기술블로그 댓글1", 0L);
        TechComment techComment2 = createTechComment(techArticle, member, techComment1, techComment1, "기술블로그 댓글2", 1L);
        TechComment techComment3 = createTechComment(techArticle, member, techComment1, techComment2, "기술블로그 댓글3", 2L);
        TechComment techComment4 = createTechComment(techArticle, member, techComment1, techComment3, "기술블로그 댓글4", 3L);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 5, 1, 0, 0, 0, 0)));
        techCommentRepository.save(techComment1);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 6, 1, 0, 0, 0, 0)));
        techCommentRepository.save(techComment2);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 7, 1, 0, 0, 0, 0)));
        techCommentRepository.save(techComment3);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 8, 1, 0, 0, 0, 0)));
        techCommentRepository.save(techComment4);

        Pageable pageable = PageRequest.of(0, 6);

        // 첫 번째 페이지
        // when1
        Long techCommentId = techComment4.getId() + 1L;
        Long pickCommentId = pickComment4.getId() + 1L;

        MyWrittenCommentRequest myWrittenCommentRequest = new MyWrittenCommentRequest(pickCommentId, techCommentId,
                MyWrittenCommentFilter.ALL);

        SliceCustom<MyWrittenCommentResponse> page1 = memberService.findMyWrittenComments(pageable,
                myWrittenCommentRequest, authentication);

        // then1
        assertAll(
                () -> assertThat(page1.getTotalElements()).isEqualTo(8),
                () -> assertThat(page1.hasNext()).isEqualTo(true)
        );

        assertThat(page1.getContent()).hasSize(6)
                .extracting("postId", "postTitle", "commentId", "commentType", "commentContents",
                        "commentRecommendTotalCount", "pickOptionTitle", "pickOptionType")
                .containsExactly(
                        Tuple.tuple(techArticle.getId(), techArticle.getTitle().getTitle(), techComment4.getId(),
                                "TECH_ARTICLE", techComment4.getContents().getCommentContents(),
                                techComment4.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(techArticle.getId(), techArticle.getTitle().getTitle(), techComment3.getId(),
                                "TECH_ARTICLE", techComment3.getContents().getCommentContents(),
                                techComment3.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(techArticle.getId(), techArticle.getTitle().getTitle(), techComment2.getId(),
                                "TECH_ARTICLE", techComment2.getContents().getCommentContents(),
                                techComment2.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(techArticle.getId(), techArticle.getTitle().getTitle(), techComment1.getId(),
                                "TECH_ARTICLE", techComment1.getContents().getCommentContents(),
                                techComment1.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(pick.getId(), pick.getTitle().getTitle(), pickComment4.getId(),
                                "PICK", pickComment4.getContents().getCommentContents(),
                                pickComment4.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(pick.getId(), pick.getTitle().getTitle(), pickComment3.getId(),
                                "PICK", pickComment3.getContents().getCommentContents(),
                                pickComment3.getRecommendTotalCount().getCount(),
                                null, null)
                );

        // 두 번째 페이지
        // when2
        techCommentId = techComment1.getId();
        pickCommentId = pickComment3.getId();

        MyWrittenCommentRequest myWrittenCommentRequest2 = new MyWrittenCommentRequest(pickCommentId, techCommentId,
                MyWrittenCommentFilter.ALL);

        SliceCustom<MyWrittenCommentResponse> page2 = memberService.findMyWrittenComments(pageable,
                myWrittenCommentRequest2, authentication);

        // then2
        assertAll(
                () -> assertThat(page2.getTotalElements()).isEqualTo(8),
                () -> assertThat(page2.hasNext()).isEqualTo(false)
        );

        assertThat(page2.getContent()).hasSize(2)
                .extracting("postId", "postTitle", "commentId", "commentType", "commentContents",
                        "commentRecommendTotalCount", "pickOptionTitle", "pickOptionType")
                .containsExactly(
                        Tuple.tuple(pick.getId(), pick.getTitle().getTitle(), pickComment2.getId(),
                                "PICK", pickComment2.getContents().getCommentContents(),
                                pickComment2.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(pick.getId(), pick.getTitle().getTitle(), pickComment1.getId(),
                                "PICK", pickComment1.getContents().getCommentContents(),
                                pickComment1.getRecommendTotalCount().getCount(),
                                pickOption.getTitle().getTitle(), pickOption.getPickOptionType().name())
                );
    }

    @Test
    @DisplayName("픽픽픽, 기술블로그 댓글을 작성시간 내림차순으로 무한스크롤 방식으로 조회할 때 회원이 아니면 예외가 발생한다.")
    void findMyWrittenComments_INVALID_MEMBER_NOT_FOUND_MESSAGE() {
        // given
        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Pageable pageable = PageRequest.of(0, 6);
        MyWrittenCommentRequest myWrittenCommentRequest = new MyWrittenCommentRequest(0L, 0L,
                MyWrittenCommentFilter.ALL);

        // when // then
        assertThatThrownBy(() -> memberService.findMyWrittenComments(pageable, myWrittenCommentRequest, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("회원이 작성한 픽픽픽 댓글을 작성시간 내림차순으로 무한스크롤 방식으로 조회한다.")
    void findMyWrittenComments_PICK() {
        // given
        DateTimeProvider dateTimeProvider = mock(DateTimeProvider.class);
        AuditingHandler auditingHandler = mock(AuditingHandler.class);
        auditingHandler.setDateTimeProvider(dateTimeProvider);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 1, 1, 0, 0, 0, 0)));

        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 픽픽픽 생성
        Pick pick = createPick("픽픽픽", member);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption pickOption = createPickOption(pick, "픽픽픽 A", PickOptionType.firstPickOption);
        pickOptionRepository.save(pickOption);

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(pick, pickOption, member);
        pickVoteRepository.save(pickVote);

        // 픽픽픽 댓글 생성
        PickComment pickComment1 = createPickComment(pick, member, pickVote, null, null, "픽픽픽 댓글1", true, 0L);
        PickComment pickComment2 = createPickComment(pick, member, null, pickComment1, pickComment1, "픽픽픽 댓글2", false,
                1L);
        PickComment pickComment3 = createPickComment(pick, member, null, pickComment1, pickComment2, "픽픽픽 댓글3", false,
                2L);
        PickComment pickComment4 = createPickComment(pick, member, null, pickComment1, pickComment3, "픽픽픽 댓글4", false,
                3L);
        PickComment pickComment5 = createPickComment(pick, member, null, pickComment1, pickComment4, "픽픽픽 댓글5", false,
                4L);
        PickComment pickComment6 = createPickComment(pick, member, null, pickComment1, pickComment5, "픽픽픽 댓글6", false,
                5L);
        PickComment pickComment7 = createPickComment(pick, member, null, pickComment1, pickComment6, "픽픽픽 댓글7", false,
                6L);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 1, 1, 0, 0, 0, 0)));
        pickCommentRepository.save(pickComment1);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 2, 1, 0, 0, 0, 0)));
        pickCommentRepository.save(pickComment2);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 3, 1, 0, 0, 0, 0)));
        pickCommentRepository.save(pickComment3);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 4, 1, 0, 0, 0, 0)));
        pickCommentRepository.save(pickComment4);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 5, 1, 0, 0, 0, 0)));
        pickCommentRepository.save(pickComment5);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 6, 1, 0, 0, 0, 0)));
        pickCommentRepository.save(pickComment6);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 7, 1, 0, 0, 0, 0)));
        pickCommentRepository.save(pickComment7);

        // 기술블로그 회사 생성
        Company company = createCompany("DreamyPatisiel");
        companyRepository.save(company);

        // 기술블로그 생성
        TechArticle techArticle = createTechArticle(company, "기술블로그 제목");
        techArticleRepository.save(techArticle);

        // 기술블로그 댓글 생성
        TechComment techComment1 = createTechComment(techArticle, member, null, null, "기술블로그 댓글1", 0L);
        TechComment techComment2 = createTechComment(techArticle, member, techComment1, techComment1, "기술블로그 댓글2", 1L);
        TechComment techComment3 = createTechComment(techArticle, member, techComment1, techComment2, "기술블로그 댓글3", 2L);
        TechComment techComment4 = createTechComment(techArticle, member, techComment1, techComment3, "기술블로그 댓글4", 3L);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 5, 1, 0, 0, 0, 0)));
        techCommentRepository.save(techComment1);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 6, 1, 0, 0, 0, 0)));
        techCommentRepository.save(techComment2);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 7, 1, 0, 0, 0, 0)));
        techCommentRepository.save(techComment3);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 8, 1, 0, 0, 0, 0)));
        techCommentRepository.save(techComment4);

        Pageable pageable = PageRequest.of(0, 6);

        // 첫 번째 페이지
        Long techCommentId = techComment4.getId() + 1L;
        Long pickCommentId = pickComment7.getId() + 1L;

        MyWrittenCommentRequest myWrittenCommentRequest1 = new MyWrittenCommentRequest(pickCommentId, techCommentId,
                MyWrittenCommentFilter.PICK);

        SliceCustom<MyWrittenCommentResponse> page1 = memberService.findMyWrittenComments(pageable,
                myWrittenCommentRequest1, authentication);

        // then
        assertAll(
                () -> assertThat(page1.getTotalElements()).isEqualTo(7),
                () -> assertThat(page1.hasNext()).isEqualTo(true)
        );

        assertThat(page1.getContent()).hasSize(6)
                .extracting("postId", "postTitle", "commentId", "commentType", "commentContents",
                        "commentRecommendTotalCount", "pickOptionTitle", "pickOptionType")
                .containsExactly(
                        Tuple.tuple(pick.getId(), pick.getTitle().getTitle(), pickComment7.getId(),
                                "PICK", pickComment7.getContents().getCommentContents(),
                                pickComment7.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(pick.getId(), pick.getTitle().getTitle(), pickComment6.getId(),
                                "PICK", pickComment6.getContents().getCommentContents(),
                                pickComment6.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(pick.getId(), pick.getTitle().getTitle(), pickComment5.getId(),
                                "PICK", pickComment5.getContents().getCommentContents(),
                                pickComment5.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(pick.getId(), pick.getTitle().getTitle(), pickComment4.getId(),
                                "PICK", pickComment4.getContents().getCommentContents(),
                                pickComment4.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(pick.getId(), pick.getTitle().getTitle(), pickComment3.getId(),
                                "PICK", pickComment3.getContents().getCommentContents(),
                                pickComment3.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(pick.getId(), pick.getTitle().getTitle(), pickComment2.getId(),
                                "PICK", pickComment2.getContents().getCommentContents(),
                                pickComment2.getRecommendTotalCount().getCount(),
                                null, null)
                );

        pickCommentId = pickComment2.getId();

        MyWrittenCommentRequest myWrittenCommentRequest2 = new MyWrittenCommentRequest(pickCommentId, techCommentId,
                MyWrittenCommentFilter.PICK);

        SliceCustom<MyWrittenCommentResponse> page2 = memberService.findMyWrittenComments(pageable,
                myWrittenCommentRequest2, authentication);

        // then
        assertAll(
                () -> assertThat(page2.getTotalElements()).isEqualTo(7),
                () -> assertThat(page2.hasNext()).isEqualTo(false)
        );

        assertThat(page2.getContent()).hasSize(1)
                .extracting("postId", "postTitle", "commentId", "commentType", "commentContents",
                        "commentRecommendTotalCount", "pickOptionTitle", "pickOptionType")
                .containsExactly(
                        Tuple.tuple(pick.getId(), pick.getTitle().getTitle(), pickComment1.getId(),
                                "PICK", pickComment1.getContents().getCommentContents(),
                                pickComment1.getRecommendTotalCount().getCount(),
                                pickOption.getTitle().getTitle(), pickOption.getPickOptionType().name())
                );
    }

    @Test
    @DisplayName("회원이 작성한 기술블로그 댓글을 작성시간 내림차순으로 무한스크롤 방식으로 조회한다.")
    void findMyWrittenComments_TECH() {
        // given
        DateTimeProvider dateTimeProvider = mock(DateTimeProvider.class);
        AuditingHandler auditingHandler = mock(AuditingHandler.class);
        auditingHandler.setDateTimeProvider(dateTimeProvider);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 1, 1, 0, 0, 0, 0)));

        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 픽픽픽 생성
        Pick pick = createPick("픽픽픽", member);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption pickOption = createPickOption(pick, "픽픽픽 A", PickOptionType.firstPickOption);
        pickOptionRepository.save(pickOption);

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(pick, pickOption, member);
        pickVoteRepository.save(pickVote);

        // 픽픽픽 댓글 생성
        PickComment pickComment1 = createPickComment(pick, member, pickVote, null, null, "픽픽픽 댓글1", true, 0L);
        PickComment pickComment2 = createPickComment(pick, member, null, pickComment1, pickComment1, "픽픽픽 댓글2", false,
                1L);
        PickComment pickComment3 = createPickComment(pick, member, null, pickComment1, pickComment2, "픽픽픽 댓글3", false,
                2L);
        PickComment pickComment4 = createPickComment(pick, member, null, pickComment1, pickComment3, "픽픽픽 댓글4", false,
                3L);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 1, 1, 0, 0, 0, 0)));
        pickCommentRepository.save(pickComment1);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 2, 1, 0, 0, 0, 0)));
        pickCommentRepository.save(pickComment2);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 3, 1, 0, 0, 0, 0)));
        pickCommentRepository.save(pickComment3);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 4, 1, 0, 0, 0, 0)));
        pickCommentRepository.save(pickComment4);

        // 기술블로그 회사 생성
        Company company = createCompany("DreamyPatisiel");
        companyRepository.save(company);

        // 기술블로그 생성
        TechArticle techArticle = createTechArticle(company, "기술블로그 제목");
        techArticleRepository.save(techArticle);

        // 기술블로그 댓글 생성
        TechComment techComment1 = createTechComment(techArticle, member, null, null, "기술블로그 댓글1", 0L);
        TechComment techComment2 = createTechComment(techArticle, member, techComment1, techComment1, "기술블로그 댓글2", 1L);
        TechComment techComment3 = createTechComment(techArticle, member, techComment1, techComment2, "기술블로그 댓글3", 2L);
        TechComment techComment4 = createTechComment(techArticle, member, techComment1, techComment3, "기술블로그 댓글4", 3L);
        TechComment techComment5 = createTechComment(techArticle, member, techComment1, techComment4, "기술블로그 댓글5", 4L);
        TechComment techComment6 = createTechComment(techArticle, member, techComment1, techComment5, "기술블로그 댓글6", 5L);
        TechComment techComment7 = createTechComment(techArticle, member, techComment1, techComment6, "기술블로그 댓글7", 6L);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 5, 1, 0, 0, 0, 0)));
        techCommentRepository.save(techComment1);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 6, 1, 0, 0, 0, 0)));
        techCommentRepository.save(techComment2);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 7, 1, 0, 0, 0, 0)));
        techCommentRepository.save(techComment3);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 8, 1, 0, 0, 0, 0)));
        techCommentRepository.save(techComment4);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 9, 1, 0, 0, 0, 0)));
        techCommentRepository.save(techComment5);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 10, 1, 0, 0, 0, 0)));
        techCommentRepository.save(techComment6);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 11, 1, 0, 0, 0, 0)));
        techCommentRepository.save(techComment7);

        Pageable pageable = PageRequest.of(0, 6);

        // 첫 번째 페이지
        // when1
        Long techCommentId = techComment7.getId() + 1L;
        Long pickCommentId = pickComment4.getId() + 1L;

        MyWrittenCommentRequest myWrittenCommentRequest1 = new MyWrittenCommentRequest(pickCommentId, techCommentId,
                MyWrittenCommentFilter.TECH_ARTICLE);

        SliceCustom<MyWrittenCommentResponse> page1 = memberService.findMyWrittenComments(pageable,
                myWrittenCommentRequest1, authentication);

        // then1
        assertAll(
                () -> assertThat(page1.getTotalElements()).isEqualTo(7),
                () -> assertThat(page1.hasNext()).isEqualTo(true)
        );

        assertThat(page1.getContent()).hasSize(6)
                .extracting("postId", "postTitle", "commentId", "commentType", "commentContents",
                        "commentRecommendTotalCount", "pickOptionTitle", "pickOptionType")
                .containsExactly(
                        Tuple.tuple(techArticle.getId(), techArticle.getTitle().getTitle(), techComment7.getId(),
                                "TECH_ARTICLE", techComment7.getContents().getCommentContents(),
                                techComment7.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(techArticle.getId(), techArticle.getTitle().getTitle(), techComment6.getId(),
                                "TECH_ARTICLE", techComment6.getContents().getCommentContents(),
                                techComment6.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(techArticle.getId(), techArticle.getTitle().getTitle(), techComment5.getId(),
                                "TECH_ARTICLE", techComment5.getContents().getCommentContents(),
                                techComment5.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(techArticle.getId(), techArticle.getTitle().getTitle(), techComment4.getId(),
                                "TECH_ARTICLE", techComment4.getContents().getCommentContents(),
                                techComment4.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(techArticle.getId(), techArticle.getTitle().getTitle(), techComment3.getId(),
                                "TECH_ARTICLE", techComment3.getContents().getCommentContents(),
                                techComment3.getRecommendTotalCount().getCount(),
                                null, null),
                        Tuple.tuple(techArticle.getId(), techArticle.getTitle().getTitle(), techComment2.getId(),
                                "TECH_ARTICLE", techComment2.getContents().getCommentContents(),
                                techComment2.getRecommendTotalCount().getCount(),
                                null, null)
                );

        // 두 번째 페이지
        // when2
        techCommentId = techComment2.getId();

        MyWrittenCommentRequest myWrittenCommentRequest2 = new MyWrittenCommentRequest(pickCommentId, techCommentId,
                MyWrittenCommentFilter.TECH_ARTICLE);

        SliceCustom<MyWrittenCommentResponse> page2 = memberService.findMyWrittenComments(pageable,
                myWrittenCommentRequest2, authentication);

        // then2
        assertAll(
                () -> assertThat(page2.getTotalElements()).isEqualTo(7),
                () -> assertThat(page2.hasNext()).isEqualTo(false)
        );

        assertThat(page2.getContent()).hasSize(1)
                .extracting("postId", "postTitle", "commentId", "commentType", "commentContents",
                        "commentRecommendTotalCount", "pickOptionTitle", "pickOptionType")
                .containsExactly(
                        Tuple.tuple(techArticle.getId(), techArticle.getTitle().getTitle(), techComment1.getId(),
                                "TECH_ARTICLE", techComment1.getContents().getCommentContents(),
                                techComment1.getRecommendTotalCount().getCount(),
                                null, null)
                );
    }

    @Test
    @DisplayName("회원이 커서 방식으로 자신이 구독한 기업 목록을 조회하여 응답을 생성한다.")
    void findMySubscribedCompanies() {
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

        // 회사 생성
        Company company1 = createCompany("Toss", "https://toss.tech",
                "https://toss.im/career/jobs", "https://company.net/image.png", "토스", "금융");
        Company company2 = createCompany("우아한 형제들", "https://techblog.woowahan.com",
                "https://career.woowahan.com", "https://company.net/image.png", "우아한 형제들", "푸드");
        Company company3 = createCompany("AWS", "https://aws.amazon.com/ko/blogs/tech",
                "https://aws.amazon.com/ko/careers", "https://company.net/image.png", "AWS", "클라우드");
        Company company4 = createCompany("채널톡", "https://channel.io/ko/blog",
                "https://channel.io/ko/jobs", "https://company.net/image.png", "채널톡", "채팅");

        List<Company> companies = List.of(company1, company2, company3, company4);
        companyRepository.saveAll(companies);

        // 회원 구독
        Subscription subscription1 = Subscription.create(member, company1);
        Subscription subscription2 = Subscription.create(member, company2);
        List<Subscription> subscriptions = List.of(subscription1, subscription2);
        subscriptionRepository.saveAll(subscriptions);

        em.flush();
        em.clear();

        // when
        SliceCustom<SubscribedCompanyResponse> mySubscribedCompanies = memberService.findMySubscribedCompanies(pageable, null, authentication);

        // then
        assertThat(mySubscribedCompanies)
                .hasSize(pageable.getPageSize())
                .extracting(
                        SubscribedCompanyResponse::getCompanyId,
                        SubscribedCompanyResponse::getCompanyName,
                        SubscribedCompanyResponse::getCompanyImageUrl,
                        SubscribedCompanyResponse::getIsSubscribed)
                .containsExactly(
                        Tuple.tuple(company2.getId(), company2.getName().getCompanyName(), company2.getOfficialImageUrl().getUrl(), true)
                );
    }

    @Test
    @DisplayName("회원이 커서 방식으로 다음페이지의 자신이 구독한 기업 목록을 조회하여 응답을 생성한다.")
    void findMySubscribedCompaniesByCursor() {
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

        // 회사 생성
        Company company1 = createCompany("Toss", "https://toss.tech",
                "https://toss.im/career/jobs", "https://company.net/image.png", "토스", "금융");
        Company company2 = createCompany("우아한 형제들", "https://techblog.woowahan.com",
                "https://career.woowahan.com", "https://company.net/image.png", "우아한 형제들", "푸드");
        Company company3 = createCompany("AWS", "https://aws.amazon.com/ko/blogs/tech",
                "https://aws.amazon.com/ko/careers", "https://company.net/image.png", "AWS", "클라우드");
        Company company4 = createCompany("채널톡", "https://channel.io/ko/blog",
                "https://channel.io/ko/jobs", "https://company.net/image.png", "채널톡", "채팅");

        List<Company> companies = List.of(company1, company2, company3, company4);
        companyRepository.saveAll(companies);

        // 회원 구독
        Subscription subscription1 = Subscription.create(member, company1);
        Subscription subscription2 = Subscription.create(member, company2);
        List<Subscription> subscriptions = List.of(subscription1, subscription2);
        subscriptionRepository.saveAll(subscriptions);

        em.flush();
        em.clear();

        // when
        SliceCustom<SubscribedCompanyResponse> mySubscribedCompanies = memberService.findMySubscribedCompanies(pageable, company2.getId(), authentication);

        // then
        assertThat(mySubscribedCompanies)
                .hasSize(pageable.getPageSize())
                .extracting(
                        SubscribedCompanyResponse::getCompanyId,
                        SubscribedCompanyResponse::getCompanyName,
                        SubscribedCompanyResponse::getCompanyImageUrl,
                        SubscribedCompanyResponse::getIsSubscribed)
                .containsExactly(
                        Tuple.tuple(company1.getId(), company1.getName().getCompanyName(), company1.getOfficialImageUrl().getUrl(), true)
                        );
    }

    @Test
    @DisplayName("회원이 커서 방식으로 자신이 구독한 기업 목록을 조회할 때 회원이 없으면 예외가 발생한다.")
    void findMySubscribedCompaniesNotFoundMemberException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(
                () -> memberService.findMySubscribedCompanies(pageable, null, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("회원은 닉네임을 변경할 수 있다.")
    void changeNickname() {
        // given
        String oldNickname = "이전 닉네임";
        String newNickname = "변경된 닉네임";
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, oldNickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);
        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when
        memberService.changeNickname(newNickname, authentication);

        // then
        assertThat(member.getNickname().getNickname()).isEqualTo(newNickname);
    }

    @DisplayName("회원이 24시간 이내에 닉네임을 변경한 적이 있다면 예외가 발생한다.")
    @ParameterizedTest
    @CsvSource({
            "0, true",
            "1, true",
            "23, true",
            "24, false", // 변경 허용
            "25, false" // 변경 허용
    })
    void changeNicknameThrowsExceptionWhenChangedWithin24Hours(long hoursAgo, boolean shouldThrowException) {
        // given
        String oldNickname = "이전 닉네임";
        String newNickname = "새 닉네임";
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, oldNickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.changeNickname(oldNickname, LocalDateTime.now().minusHours(hoursAgo));
        memberRepository.save(member);
        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        if (shouldThrowException) {
            assertThatThrownBy(() -> memberService.changeNickname(newNickname, authentication))
                    .isInstanceOf(NicknameException.class)
                    .hasMessageContaining(NicknameExceptionMessage.NICKNAME_CHANGE_RATE_LIMIT_MESSAGE);
        } else {
            assertThatCode(() -> memberService.changeNickname(newNickname, authentication))
                    .doesNotThrowAnyException();
            assertThat(member.getNickname().getNickname()).isEqualTo(newNickname);
        }
    }

    private static Company createCompany(String companyName, String officialUrl, String careerUrl,
                                         String imageUrl, String description, String industry) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .careerUrl(new Url(careerUrl))
                .officialUrl(new Url(officialUrl))
                .officialImageUrl(new Url(imageUrl))
                .description(description)
                .industry(industry)
                .build();
    }

    private static TechComment createTechComment(TechArticle techArticle, Member member, TechComment originParent,
                                                 TechComment parent, String contents, Long recommendTotalCount) {
        return TechComment.builder()
                .techArticle(techArticle)
                .createdBy(member)
                .originParent(originParent)
                .parent(parent)
                .contents(new CommentContents(contents))
                .recommendTotalCount(new Count(recommendTotalCount))
                .build();
    }

    private static TechArticle createTechArticle(Company company, String title) {
        return TechArticle.builder()
                .company(company)
                .title(new Title(title))
                .build();
    }

    private static Company createCompany(String companyName) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .build();
    }

    private static PickOption createPickOption(Pick pick, String title, PickOptionType pickOptionType) {
        return PickOption.builder()
                .pick(pick)
                .title(new Title(title))
                .pickOptionType(pickOptionType)
                .build();
    }

    private static Pick createPick(String title, Member member) {
        return Pick.builder()
                .title(new Title(title))
                .member(member)
                .contentStatus(ContentStatus.APPROVAL)
                .build();
    }

    private static PickComment createPickComment(Pick pick, Member member, PickVote pickVote, PickComment originParent,
                                                 PickComment parent, String contents, Boolean isPublic,
                                                 Long recommendTotalCount) {
        return PickComment.builder()
                .pick(pick)
                .createdBy(member)
                .pickVote(pickVote)
                .originParent(originParent)
                .parent(parent)
                .contents(new CommentContents(contents))
                .isPublic(isPublic)
                .recommendTotalCount(new Count(recommendTotalCount))
                .build();
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