package com.dreamypatisiel.devdevdev.domain.service.member;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.MEMBER_INCOMPLETE_SURVEY_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyAnswer;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyQuestion;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyQuestionOption;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyVersionQuestionMapper;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CustomSurveyAnswer;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.comment.CommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.comment.MyWrittenCommentDto;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyAnswerRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyQuestionOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyVersionQuestionMapperRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.custom.SurveyAnswerJdbcTemplateRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.TechArticleCommonService;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.exception.SurveyException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.comment.MyWrittenCommentFilter;
import com.dreamypatisiel.devdevdev.web.dto.request.comment.MyWrittenCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.member.RecordMemberExitSurveyAnswerRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.member.RecordMemberExitSurveyQuestionOptionsRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.comment.MyWrittenCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.member.MemberExitSurveyQuestionResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.member.MemberExitSurveyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.MyPickMainResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.subscription.SubscribedCompanyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.CompanyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    private final PickRepository pickRepository;
    private final SurveyVersionQuestionMapperRepository surveyVersionQuestionMapperRepository;
    private final SurveyAnswerRepository surveyAnswerRepository;
    private final TechArticleRepository techArticleRepository;
    private final TechArticleCommonService techArticleCommonService;
    private final TimeProvider timeProvider;
    private final SurveyQuestionOptionRepository surveyQuestionOptionRepository;
    private final SurveyAnswerJdbcTemplateRepository surveyAnswerJdbcTemplateRepository;
    private final CommentRepository commentRepository;
    private final CompanyRepository companyRepository;

    /**
     * 회원 탈퇴 회원의 북마크와 회원 정보를 삭제합니다.
     */
    @Transactional
    public void deleteMember(Authentication authentication) {
        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 탈퇴 서베이 확인
        // mapper 조회(oneTo 관계인 surveyVersion, surveyQuestion 페치조인)
        List<SurveyVersionQuestionMapper> surveyVersionQuestionMappers = surveyVersionQuestionMapperRepository.findMapperWithVersionAndQuestion();

        // surveyQuestions 추출
        List<Long> surveyQuestionIds = surveyVersionQuestionMappers.stream()
                .map(SurveyVersionQuestionMapper::getSurveyQuestion)
                .map(SurveyQuestion::getId)
                .toList();

        // 모든 surveyQuestions에 대해 해당 Member의 surveyAnswer이 있는지 확인
        boolean hasAnsweredAllQuestions = surveyAnswerRepository.existsByMemberAndIdIn(member, surveyQuestionIds);

        // 회원 삭제
        if (!hasAnsweredAllQuestions) {
            throw new SurveyException(MEMBER_INCOMPLETE_SURVEY_MESSAGE);
        }
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

        // 전체 갯수
        Long totalElements = pickRepository.countByMember(findMember);

        // 데이터 가공
        List<MyPickMainResponse> myPickMainsResponse = findPicks.stream()
                .map(MyPickMainResponse::from)
                .toList();

        return new SliceCustom<>(myPickMainsResponse, pageable, totalElements);
    }

    /**
     * @Note: survey_version 1:N survey_version_question_mapper N:1 survey_question 서베이 버전에 맞는 서베이 목록을 조회합니다.
     * @Author: 장세웅
     * @Since: 2024.05.26
     */
    public MemberExitSurveyResponse findMemberExitSurvey(Authentication authentication) {

        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // mapper 조회(oneTo 관계인 surveyVersion, surveyQuestion 페치조인)
        List<SurveyVersionQuestionMapper> surveyVersionQuestionMappers = surveyVersionQuestionMapperRepository.findMapperWithVersionAndQuestion();

        // surveyQuestions 추출
        List<SurveyQuestion> surveyQuestions = surveyVersionQuestionMappers.stream()
                .map(SurveyVersionQuestionMapper::getSurveyQuestion)
                .toList();

        // question, questionOption 데이터 가공
        List<MemberExitSurveyQuestionResponse> memberExitSurveyQuestionsResponse = surveyQuestions.stream()
                .map(question -> MemberExitSurveyQuestionResponse.of(question, findMember.getNickname()))
                .toList();

        // 현재 적용된 surveyVersionId 추출
        Long surveyVersionId = surveyVersionQuestionMappers.getFirst().getSurveyVersion().getId();

        return MemberExitSurveyResponse.of(surveyVersionId, memberExitSurveyQuestionsResponse);
    }

    /**
     * @Note: jdbcTemplate 을 사용해서 bulk insert 를 사용했습니다. JPA 는 bulk insert 를 지원하지 않는 것으로 알고 있습니다.
     * @Author: 장세웅
     * @Since: 24.06.03
     */
    @Transactional
    public void recordMemberExitSurveyAnswer(RecordMemberExitSurveyAnswerRequest recordMemberExitSurveyAnswerRequest,
                                             Authentication authentication) {

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        Long questionId = recordMemberExitSurveyAnswerRequest.getQuestionId();
        List<RecordMemberExitSurveyQuestionOptionsRequest> memberExitSurveyQuestionOptions = recordMemberExitSurveyAnswerRequest.getMemberExitSurveyQuestionOptions();

        // questionOptions 요청을 Map으로 변환
        Map<Long, RecordMemberExitSurveyQuestionOptionsRequest> surveyQuestionOptions = RecordMemberExitSurveyQuestionOptionsRequest.convertToMap(
                memberExitSurveyQuestionOptions);

        // questionOptions 요청에서 id만 추출
        List<Long> ids = RecordMemberExitSurveyQuestionOptionsRequest.convertToIds(memberExitSurveyQuestionOptions);

        // 질문 선택지 조회(toOne question 페치 조인)
        List<SurveyQuestionOption> findSurveyQuestionOptions = surveyQuestionOptionRepository.findWithQuestionByIdInAndSurveyQuestionId(
                ids, questionId);

        List<SurveyAnswer> surveyAnswers = findSurveyQuestionOptions.stream()
                .map(option -> createSurveyAnswerBy(option, surveyQuestionOptions, findMember))
                .filter(Objects::nonNull) // null 이 아닌 것만 반환
                .toList();

        // surveyAnswer 벌크 저장
        surveyAnswerJdbcTemplateRepository.saveAll(surveyAnswers);
    }

    private SurveyAnswer createSurveyAnswerBy(SurveyQuestionOption option,
                                              Map<Long, RecordMemberExitSurveyQuestionOptionsRequest> surveyQuestionOptions,
                                              Member findMember) {

        if (!surveyQuestionOptions.containsKey(option.getId())) {
            return null;
        }

        // surveyQuestionOptions 에 알맞은 키의 값이 존재하면
        String message = surveyQuestionOptions.get(option.getId()).getMessage();
        SurveyQuestion surveyQuestion = option.getSurveyQuestion();

        // message 가 null 이면
        if (message == null) {
            // customMessage 가 없는 SurveyAnswer 생성
            return SurveyAnswer.createWithoutCustomMessage(findMember, surveyQuestion, option);
        }

        // customMessage 가 있는 SurveyAnswer 생성
        return SurveyAnswer.create(new CustomSurveyAnswer(message), findMember,
                surveyQuestion, option);
    }

    /**
     * 회원 자신이 북마크한 기술블로그를 조회합니다.
     */
    public Slice<TechArticleMainResponse> getBookmarkedTechArticles(Pageable pageable, Long techArticleId,
                                                                    BookmarkSort bookmarkSort,
                                                                    Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 북마크 기술블로그 조회(rds, elasticsearch)
        Slice<TechArticle> techArticleSlices = techArticleRepository.findBookmarkedByMemberAndCursor(pageable,
                techArticleId, bookmarkSort, findMember);

        List<TechArticle> techArticles = techArticleSlices.getContent();

        List<ElasticTechArticle> elasticTechArticles = techArticleCommonService.findElasticTechArticlesByTechArticles(
                techArticles);

        // 데이터 가공
        List<TechArticleMainResponse> techArticleMainResponse = techArticles.stream()
                .flatMap(techArticle -> mapToTechArticlesResponse(techArticle, elasticTechArticles, findMember))
                .toList();

        return new SliceImpl<>(techArticleMainResponse, pageable, techArticleSlices.hasNext());
    }

    /**
     * 기술블로그 목록 응답 형태로 가공합니다.
     */
    private Stream<TechArticleMainResponse> mapToTechArticlesResponse(TechArticle techArticle,
                                                                      List<ElasticTechArticle> elasticTechArticles,
                                                                      Member member) {
        return elasticTechArticles.stream()
                .filter(elasticTechArticle -> techArticle.getElasticId().equals(elasticTechArticle.getId()))
                .map(elasticTechArticle -> TechArticleMainResponse.of(techArticle, elasticTechArticle,
                        CompanyResponse.from(techArticle.getCompany()), member));
    }

    /**
     * @Note: 회원이 작성한 댓글을 조회합니다.(삭제된 댓글 미포함)
     * @Author: 장세웅
     * @Since: 2024.12.31
     */
    public SliceCustom<MyWrittenCommentResponse> findMyWrittenComments(Pageable pageable,
                                                                       MyWrittenCommentRequest myWrittenCommentRequest,
                                                                       Authentication authentication) {

        Long pickCommentId = myWrittenCommentRequest.getPickCommentId();
        Long techCommentId = myWrittenCommentRequest.getTechCommentId();
        MyWrittenCommentFilter myWrittenCommentSort = myWrittenCommentRequest.getCommentFilter();

        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 회원이 작성한 댓글 조회
        SliceCustom<MyWrittenCommentDto> findMyWrittenCommentsDto = commentRepository.findMyWrittenCommentsByCursor(
                findMember.getId(), pickCommentId, techCommentId, myWrittenCommentSort, pageable);

        // 데이터 가공
        List<MyWrittenCommentResponse> myWrittenCommentResponses = MyWrittenCommentResponse.from(
                findMyWrittenCommentsDto.getContent());

        boolean hasNext = findMyWrittenCommentsDto.hasNext();
        long totalElements = findMyWrittenCommentsDto.getTotalElements();

        return new SliceCustom<>(myWrittenCommentResponses, pageable, hasNext, totalElements);
    }

    /**
     * @Note: 회원이 구독한 기업 목록을 조회합니다.
     * @Author: 유소영
     * @Since: 2025.03.23
     */
    public SliceCustom<SubscribedCompanyResponse> findMySubscribedCompanies(Pageable pageable,
                                                                            Long companyId,
                                                                            Authentication authentication) {
        // 회원 조회
        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // 회원이 구독한 기업 목록 조회(구독 조인)
        SliceCustom<Company> subscribedCompanies = companyRepository.findSubscribedCompaniesByMemberByCursor(pageable,
                companyId, findMember.getId());

        // 데이터 가공
        List<SubscribedCompanyResponse> subscribedCompanyResponses = subscribedCompanies.getContent().stream().map(
                company -> {
                    return SubscribedCompanyResponse.createWithIsSubscribed(company, true);
                }).collect(Collectors.toList());

        return new SliceCustom<>(subscribedCompanyResponses, pageable, subscribedCompanies.getTotalElements());
    }
}
