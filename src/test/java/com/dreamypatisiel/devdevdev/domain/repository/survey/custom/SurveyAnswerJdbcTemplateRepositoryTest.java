package com.dreamypatisiel.devdevdev.domain.repository.survey.custom;

import static org.assertj.core.api.Assertions.assertThat;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyAnswer;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyQuestion;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyQuestionOption;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CustomSurveyAnswer;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyAnswerRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyQuestionOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyQuestionRepository;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class SurveyAnswerJdbcTemplateRepositoryTest {

    @Autowired
    SurveyAnswerRepository surveyAnswerRepository;
    @Autowired
    SurveyAnswerJdbcTemplateRepository surveyAnswerJdbcTemplateRepository;
    @Autowired
    SurveyQuestionRepository surveyQuestionRepository;
    @Autowired
    SurveyQuestionOptionRepository surveyQuestionOptionRepository;
    @Autowired
    MemberRepository memberRepository;

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

    @Test
    @DisplayName("회원탈퇴 서베이 결과를 벌크로 저장한다.")
    void saveAll() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 서베이 질문 생성
        SurveyQuestion question = SurveyQuestion.builder()
                .title("#nickName님 회원 탈퇴하는 이유를 알려주세요.")
                .content("회원 탈퇴하는 이유를 상세하게 알려주세요.")
                .sortOrder(0)
                .build();
        surveyQuestionRepository.save(question);

        // 서베이 질문 옵션 생성
        SurveyQuestionOption option = SurveyQuestionOption.builder()
                .title("채용정보가 부족해요.")
                .sortOrder(0)
                .build();
        option.changeSurveyQuestion(question);
        surveyQuestionOptionRepository.save(option);

        List<SurveyAnswer> surveyAnswers = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            SurveyAnswer surveyAnswer = create("customMessage " + i, member, question, option);
            SurveyAnswer surveyAnswerWithoutCustomMessage = createWithoutCustomMessage(member, question, option);

            surveyAnswers.add(surveyAnswer);
            surveyAnswers.add(surveyAnswerWithoutCustomMessage);
        }
        // when
        long before = System.currentTimeMillis();

        surveyAnswerJdbcTemplateRepository.saveAll(surveyAnswers);

        long after = System.currentTimeMillis();

        // then
        System.out.println("걸린시간= " + (after - before) + "ms");

        int size = surveyAnswerRepository.findAll().size();
        assertThat(size).isEqualTo(1_000);
    }

    private SurveyAnswer create(String customMessage, Member member, SurveyQuestion surveyQuestion,
                                SurveyQuestionOption surveyQuestionOption) {
        return SurveyAnswer.builder()
                .customMessage(new CustomSurveyAnswer(customMessage))
                .member(member)
                .surveyQuestion(surveyQuestion)
                .surveyQuestionOption(surveyQuestionOption)
                .build();
    }

    private SurveyAnswer createWithoutCustomMessage(Member member, SurveyQuestion surveyQuestion,
                                                    SurveyQuestionOption surveyQuestionOption) {
        return SurveyAnswer.builder()
                .member(member)
                .surveyQuestion(surveyQuestion)
                .surveyQuestionOption(surveyQuestionOption)
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
}