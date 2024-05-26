package com.dreamypatisiel.devdevdev.domain.service.member;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyQuestion;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyVersionQuestionMapper;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyQuestionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyVersionQuestionMapperRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.MemberExitSurveyQuestionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.MemberExitSurveyResponse;
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
    private final PickRepository pickRepository;
    private final SurveyVersionQuestionMapperRepository surveyVersionQuestionMapperRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
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
                .map(MyPickMainResponse::from)
                .toList();

        return new SliceImpl<>(myPickMainsResponse, pageable, findPicks.hasNext());
    }

    /**
     * @Note: survey_version 1:N survey_version_question_mapper N:1 survey_question 서베이 버전에 맞ㅡㄴ
     * @Author: 장세웅
     * @Since: 2024.05.26
     */
    public MemberExitSurveyResponse findMemberExitSurvey(Authentication authentication) {

        Member findMember = memberProvider.getMemberByAuthentication(authentication);

        // mapper 조회(oneTo 관계인 surveyVersion, surveyQuestion 페치조인)
        List<SurveyVersionQuestionMapper> surveyVersionQuestionMappers = surveyVersionQuestionMapperRepository.findMapperWithVersionAndQuestionByVersionName();

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
}
