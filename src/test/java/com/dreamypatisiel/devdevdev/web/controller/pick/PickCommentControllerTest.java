package com.dreamypatisiel.devdevdev.web.controller.pick;

import static com.dreamypatisiel.devdevdev.web.dto.response.ResultType.SUCCESS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazonaws.services.s3.AmazonS3;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.PickCommentRecommend;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.controller.SupportControllerTest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickRepliedCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class PickCommentControllerTest extends SupportControllerTest {

    @Autowired
    PickRepository pickRepository;
    @Autowired
    PickOptionRepository pickOptionRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PickPopularScorePolicy pickPopularScorePolicy;
    @Autowired
    PickOptionImageRepository pickOptionImageRepository;
    @Autowired
    PickVoteRepository pickVoteRepository;
    @Autowired
    PickCommentRepository pickCommentRepository;
    @Autowired
    PickCommentRecommendRepository pickCommentRecommendRepository;
    @Autowired
    EntityManager em;
    @MockBean
    AmazonS3 amazonS3Client;

    @Test
    @DisplayName("회원이 승인 상태의 픽픽픽에 댓글을 작성한다.")
    void registerPickComment() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), new Count(0L), new Count(0L), new Count(0L), new Count(0L),
                ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션2 타이틀"),
                new PickOptionContents("픽픽픽 옵션2 컨텐츠"), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 이미지 생성
        PickOptionImage firstPickOptionImage = createPickOptionImage("firstPickOptionImage", firstPickOption);
        PickOptionImage secondPickOptionImage = createPickOptionImage("secondPickOptionImage", firstPickOption);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(member, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        RegisterPickCommentRequest registerPickCommentRequest = new RegisterPickCommentRequest("안녕하세웅",
                true);

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/picks/{pickId}/comments", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(registerPickCommentRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.pickCommentId").isNumber());
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("회원이 승인 상태의 픽픽픽에 댓글을 작성할 때 픽픽픽 공개 여부가 null이면 예외가 발생한다.")
    void registerPickCommentBindExceptionPickVotePublicIsNull(Boolean isPickVotePublic) throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션2 타이틀"),
                new PickOptionContents("픽픽픽 옵션2 컨텐츠"), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 이미지 생성
        PickOptionImage firstPickOptionImage = createPickOptionImage("firstPickOptionImage", firstPickOption);
        PickOptionImage secondPickOptionImage = createPickOptionImage("secondPickOptionImage", firstPickOption);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(member, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        RegisterPickCommentRequest registerPickCommentRequest = new RegisterPickCommentRequest("안녕하세웅",
                isPickVotePublic);

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/picks/{pickId}/comments", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(registerPickCommentRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @ParameterizedTest
    @EmptySource
    @DisplayName("회원이 승인 상태의 픽픽픽에 댓글을 작성할 때 픽픽픽 댓글 내용이 공백이면 예외가 발생한다.")
    void registerPickCommentBindExceptionPickOptionIdIsNull(String contents) throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션2 타이틀"),
                new PickOptionContents("픽픽픽 옵션2 컨텐츠"), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 이미지 생성
        PickOptionImage firstPickOptionImage = createPickOptionImage("firstPickOptionImage", firstPickOption);
        PickOptionImage secondPickOptionImage = createPickOptionImage("secondPickOptionImage", firstPickOption);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(member, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        RegisterPickCommentRequest registerPickCommentRequest = new RegisterPickCommentRequest(contents,
                true);

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/picks/{pickId}/comments", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(registerPickCommentRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("픽픽픽 답글을 작성한다.")
    void registerPickRepliedComment() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                "꿈빛맛티시엘", password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), new Count(0L), new Count(0L), new Count(0L), new Count(0L),
                ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 생성
        PickComment replidPickComment = createReplidPickComment(new CommentContents("댓글1의 답글1"), false, member, pick,
                pickComment, pickComment);
        pickCommentRepository.save(replidPickComment);

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");

        // when // then
        mockMvc.perform(
                        post("/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentOriginParentId}/{pickParentCommentId}",
                                pick.getId(), pickComment.getId(), replidPickComment.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.pickCommentId").isNumber());
    }

    @ParameterizedTest
    @EmptySource
    @DisplayName("픽픽픽 답글을 작성할 때 답글 내용이 공백이면 예외가 발생한다.")
    void registerPickRepliedCommentBindException(String contents) throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                "꿈빛맛티시엘", password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 생성
        PickComment replidPickComment = createReplidPickComment(new CommentContents("댓글1의 답글1"), false, member, pick,
                pickComment, pickComment);
        pickCommentRepository.save(replidPickComment);

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest(contents);

        // when // then
        mockMvc.perform(
                        post("/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentOriginParentId}/{pickParentCommentId}",
                                pick.getId(), pickComment.getId(), replidPickComment.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("승인 상태이고 회원 본인이 작성한 삭제되지 않은 픽픽픽 댓글을 수정한다.")
    void modifyPickComment() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        ModifyPickCommentRequest request = new ModifyPickCommentRequest("주무세웅");

        // when // then
        mockMvc.perform(patch("/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentId}",
                        pick.getId(), pickComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.pickCommentId").isNumber());
    }

    @ParameterizedTest
    @EmptySource
    @DisplayName("승인 상태이고 회원 본인이 작성한 삭제되지 않은 픽픽픽 댓글을 수정할 때 수정할 내용이 공백이면 예외가 발생한다.")
    void modifyPickCommentBindException(String contents) throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        ModifyPickCommentRequest request = new ModifyPickCommentRequest(contents);

        // when // then
        mockMvc.perform(patch("/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentId}",
                        pick.getId(), pickComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("회원 본인이 작성한 승인 상태의 픽픽픽의 삭제상태가 아닌 댓글을 삭제한다.")
    void deletePickComment() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when // then
        mockMvc.perform(delete("/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentId}",
                        pick.getId(), pickComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.pickCommentId").isNumber());
    }

    @Test
    @DisplayName("픽픽픽 댓글을 삭제할 때 본인이 작성한 댓글이 아니면 예외가 발생한다.")
    void deletePickCommentOtherMemberException() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 다른 회원 생성
        SocialMemberDto otherSocialMemberDto = createSocialDto("helloWorld", "헬로월드",
                "헬로월드", "1234", "helloWorld@kakao.com", socialType, role);
        Member ohterMember = Member.createMemberBy(otherSocialMemberDto);
        memberRepository.save(ohterMember);

        // 다른 회원이 작성한 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, ohterMember, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when // then
        mockMvc.perform(delete("/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentId}",
                        pick.getId(), pickComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @ParameterizedTest
    @EnumSource(PickCommentSort.class)
    @DisplayName("픽픽픽 댓글/답글을 정렬 조건에 따라서 조회한다.")
    void getPickComments(PickCommentSort pickCommentSort) throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto1 = createSocialDto("user1", "user1", "김민영", password, "alsdudr97@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto2 = createSocialDto("user2", "user2", "이임하", password, "wlgks555@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto3 = createSocialDto("user3", "user3", "문민주", password, "mmj9908@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto4 = createSocialDto("user4", "user4", "유소영", password, "merooongg@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto5 = createSocialDto("user5", "user5", "장세웅", password, "howisitgoing@kakao.com",
                socialType, role);
        SocialMemberDto socialMemberDto6 = createSocialDto("user6", "user6", "nickname", password, "user6@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto7 = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member1 = Member.createMemberBy(socialMemberDto1);
        Member member2 = Member.createMemberBy(socialMemberDto2);
        Member member3 = Member.createMemberBy(socialMemberDto3);
        Member member4 = Member.createMemberBy(socialMemberDto4);
        Member member5 = Member.createMemberBy(socialMemberDto5);
        Member member6 = Member.createMemberBy(socialMemberDto6);
        Member member7 = Member.createMemberBy(socialMemberDto7);
        memberRepository.saveAll(List.of(member1, member2, member3, member4, member5, member6, member7));

        // 픽픽픽 생성
        Pick pick = createPick(new Title("꿈파 워크샵 어디로 갈까요?"), ContentStatus.APPROVAL, new Count(9), member1);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(new Title("휴양지의 근본 제주도!"), new Count(2), pick,
                PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(new Title("한국의 알프스 강원도!"), new Count(2), pick,
                PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 투표 생성
        PickVote member1PickVote = createPickVote(member1, firstPickOption, pick);
        PickVote member2PickVote = createPickVote(member2, firstPickOption, pick);
        PickVote member3PickVote = createPickVote(member3, secondPickOption, pick);
        PickVote member4PickVote = createPickVote(member4, secondPickOption, pick);
        pickVoteRepository.saveAll(List.of(member1PickVote, member2PickVote, member3PickVote, member4PickVote));

        // 픽픽픽 최초 댓글 생성
        PickComment originParentPickComment1 = createPickComment(new CommentContents("나는 미뇽냥녕뇽이다!"), true, new Count(2),
                new Count(2), member1, pick, member1PickVote);
        PickComment originParentPickComment2 = createPickComment(new CommentContents("임하하하하하"), true, new Count(1),
                new Count(1), member2, pick, member2PickVote);
        PickComment originParentPickComment3 = createPickComment(new CommentContents("손흥민 최고다!"), true, new Count(0),
                new Count(0), member3, pick, member3PickVote);
        PickComment originParentPickComment4 = createPickComment(new CommentContents("나는 소영소"), false, new Count(0),
                new Count(0), member4, pick, member4PickVote);
        PickComment originParentPickComment5 = createPickComment(new CommentContents("힘들면 힘을내자!"), false, new Count(0),
                new Count(0), member5, pick, null);
        PickComment originParentPickComment6 = createPickComment(new CommentContents("댓글6"), false, new Count(0),
                new Count(0), member6, pick, null);
        originParentPickComment6.changeDeletedAt(LocalDateTime.now(), member6);

        pickCommentRepository.saveAll(
                List.of(originParentPickComment6, originParentPickComment5, originParentPickComment4,
                        originParentPickComment3, originParentPickComment2, originParentPickComment1));

        // 픽픽픽 답글 생성
        PickComment pickReply1 = createReplidPickComment(new CommentContents("미냥뇽냥녕 아닌가요?!"), member2, pick,
                originParentPickComment1, originParentPickComment1);
        PickComment pickReply2 = createReplidPickComment(new CommentContents("손흥민 아닌가요?"), member3, pick,
                originParentPickComment1, pickReply1);
        PickComment pickReply3 = createReplidPickComment(new CommentContents("나는 소주소"), member4, pick,
                originParentPickComment2, originParentPickComment2);
        pickCommentRepository.saveAll(List.of(pickReply3, pickReply2, pickReply1));

        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 5);

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}/comments",
                        pick.getId())
                        .queryParam("pickCommentId", String.valueOf(Long.MAX_VALUE))
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("pickCommentSort", pickCommentSort.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].pickCommentId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].createdAt").isString())
                .andExpect(jsonPath("$.data.content.[0].memberId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].author").isString())
                .andExpect(jsonPath("$.data.content.[0].isCommentOfPickAuthor").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].isCommentAuthor").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].isRecommended").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].maskedEmail").isString())
                .andExpect(jsonPath("$.data.content.[0].votedPickOption").isString())
                .andExpect(jsonPath("$.data.content.[0].votedPickOptionTitle").isString())
                .andExpect(jsonPath("$.data.content.[0].contents").isString())
                .andExpect(jsonPath("$.data.content.[0].replyTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].recommendTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].isModified").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].isDeleted").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].pickCommentId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].memberId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].pickParentCommentId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].pickOriginParentCommentId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].createdAt").isString())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].isCommentOfPickAuthor").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].isCommentAuthor").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].isRecommended").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].author").isString())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].maskedEmail").isString())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].contents").isString())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].recommendTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].isModified").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].isDeleted").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].pickParentCommentMemberId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].pickParentCommentAuthor").isString())
                .andExpect(jsonPath("$.data.pageable").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable.pageNumber").isNumber())
                .andExpect(jsonPath("$.data.pageable.pageSize").isNumber())
                .andExpect(jsonPath("$.data.pageable.sort").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable.sort.empty").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.sorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.unsorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.offset").isNumber())
                .andExpect(jsonPath("$.data.pageable.paged").isBoolean())
                .andExpect(jsonPath("$.data.pageable.unpaged").isBoolean())
                .andExpect(jsonPath("$.data.totalElements").value(9))
                .andExpect(jsonPath("$.data.totalOriginParentComments").value(5))
                .andExpect(jsonPath("$.data.first").isBoolean())
                .andExpect(jsonPath("$.data.last").isBoolean())
                .andExpect(jsonPath("$.data.size").isNumber())
                .andExpect(jsonPath("$.data.number").isNumber())
                .andExpect(jsonPath("$.data.sort").isNotEmpty())
                .andExpect(jsonPath("$.data.sort.empty").isBoolean())
                .andExpect(jsonPath("$.data.sort.sorted").isBoolean())
                .andExpect(jsonPath("$.data.sort.unsorted").isBoolean())
                .andExpect(jsonPath("$.data.numberOfElements").isNumber())
                .andExpect(jsonPath("$.data.empty").isBoolean());
    }

    @ParameterizedTest
    @EnumSource(PickCommentSort.class)
    @DisplayName("픽픽픽 댓글/답글을 정렬 조건에 따라서 조회한다.(첫 번째 픽픽픽에 투표한 댓글만 조회)")
    void getPickCommentsFirstPickOption(PickCommentSort pickCommentSort) throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto1 = createSocialDto("user1", "user1", "김민영", password, "alsdudr97@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto2 = createSocialDto("user2", "user2", "이임하", password, "wlgks555@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto3 = createSocialDto("user3", "user3", "문민주", password, "mmj9908@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto4 = createSocialDto("user4", "user4", "유소영", password, "merooongg@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto5 = createSocialDto("user5", "user5", "장세웅", password, "howisitgoing@kakao.com",
                socialType, role);
        SocialMemberDto socialMemberDto6 = createSocialDto("user6", "user6", "nickname", password, "user6@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto7 = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member1 = Member.createMemberBy(socialMemberDto1);
        Member member2 = Member.createMemberBy(socialMemberDto2);
        Member member3 = Member.createMemberBy(socialMemberDto3);
        Member member4 = Member.createMemberBy(socialMemberDto4);
        Member member5 = Member.createMemberBy(socialMemberDto5);
        Member member6 = Member.createMemberBy(socialMemberDto6);
        Member member7 = Member.createMemberBy(socialMemberDto7);
        memberRepository.saveAll(List.of(member1, member2, member3, member4, member5, member6, member7));

        // 픽픽픽 생성
        Pick pick = createPick(new Title("꿈파 워크샵 어디로 갈까요?"), ContentStatus.APPROVAL, new Count(8), member1);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(new Title("휴양지의 근본 제주도!"), new Count(2), pick,
                PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(new Title("한국의 알프스 강원도!"), new Count(2), pick,
                PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 투표 생성
        PickVote member1PickVote = createPickVote(member1, firstPickOption, pick);
        PickVote member2PickVote = createPickVote(member2, firstPickOption, pick);
        PickVote member3PickVote = createPickVote(member3, secondPickOption, pick);
        PickVote member4PickVote = createPickVote(member4, secondPickOption, pick);
        pickVoteRepository.saveAll(List.of(member1PickVote, member2PickVote, member3PickVote, member4PickVote));

        // 픽픽픽 최초 댓글 생성
        PickComment originParentPickComment1 = createPickComment(new CommentContents("나는 미뇽냥녕뇽이다!"), true, new Count(2),
                new Count(2), member1, pick, member1PickVote);
        PickComment originParentPickComment2 = createPickComment(new CommentContents("임하하하하하"), true, new Count(1),
                new Count(1), member2, pick, member2PickVote);
        PickComment originParentPickComment3 = createPickComment(new CommentContents("손흥민 최고다!"), true, new Count(0),
                new Count(0), member3, pick, member3PickVote);
        PickComment originParentPickComment4 = createPickComment(new CommentContents("나는 소영소"), false, new Count(0),
                new Count(0), member4, pick, member4PickVote);
        PickComment originParentPickComment5 = createPickComment(new CommentContents("힘들면 힘을내자!"), false, new Count(0),
                new Count(0), member5, pick, null);
        PickComment originParentPickComment6 = createPickComment(new CommentContents("댓글6"), false, new Count(0),
                new Count(0), member6, pick, null);
        originParentPickComment6.changeDeletedAt(LocalDateTime.now(), member6);
        pickCommentRepository.saveAll(
                List.of(originParentPickComment6, originParentPickComment5, originParentPickComment4,
                        originParentPickComment3, originParentPickComment2, originParentPickComment1));

        // 픽픽픽 답글 생성
        PickComment pickReply1 = createReplidPickComment(new CommentContents("미냥뇽냥녕 아닌가요?!"), member2, pick,
                originParentPickComment1, originParentPickComment1);
        PickComment pickReply2 = createReplidPickComment(new CommentContents("손흥민 아닌가요?"), member3, pick,
                originParentPickComment1, pickReply1);
        PickComment pickReply3 = createReplidPickComment(new CommentContents("나는 소주소"), member4, pick,
                originParentPickComment2, originParentPickComment2);
        pickCommentRepository.saveAll(List.of(pickReply3, pickReply2, pickReply1));

        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 5);

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}/comments",
                        pick.getId())
                        .queryParam("pickCommentId", String.valueOf(Long.MAX_VALUE))
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("pickCommentSort", pickCommentSort.name())
                        .queryParam("pickOptionTypes", PickOptionType.firstPickOption.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].pickCommentId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].createdAt").isString())
                .andExpect(jsonPath("$.data.content.[0].memberId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].author").isString())
                .andExpect(jsonPath("$.data.content.[0].isCommentOfPickAuthor").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].isCommentAuthor").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].isRecommended").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].maskedEmail").isString())
                .andExpect(jsonPath("$.data.content.[0].votedPickOption").isString())
                .andExpect(jsonPath("$.data.content.[0].votedPickOptionTitle").isString())
                .andExpect(jsonPath("$.data.content.[0].contents").isString())
                .andExpect(jsonPath("$.data.content.[0].replyTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].recommendTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].isModified").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].isDeleted").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].pickCommentId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].memberId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].pickParentCommentId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].pickOriginParentCommentId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].createdAt").isString())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].isCommentOfPickAuthor").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].isCommentAuthor").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].isRecommended").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].author").isString())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].maskedEmail").isString())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].contents").isString())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].recommendTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].isModified").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].isDeleted").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].pickParentCommentMemberId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].pickParentCommentAuthor").isString())
                .andExpect(jsonPath("$.data.pageable").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable.pageNumber").isNumber())
                .andExpect(jsonPath("$.data.pageable.pageSize").isNumber())
                .andExpect(jsonPath("$.data.pageable.sort").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable.sort.empty").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.sorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.unsorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.offset").isNumber())
                .andExpect(jsonPath("$.data.pageable.paged").isBoolean())
                .andExpect(jsonPath("$.data.pageable.unpaged").isBoolean())
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.totalOriginParentComments").value(5))
                .andExpect(jsonPath("$.data.first").isBoolean())
                .andExpect(jsonPath("$.data.last").isBoolean())
                .andExpect(jsonPath("$.data.size").isNumber())
                .andExpect(jsonPath("$.data.number").isNumber())
                .andExpect(jsonPath("$.data.sort").isNotEmpty())
                .andExpect(jsonPath("$.data.sort.empty").isBoolean())
                .andExpect(jsonPath("$.data.sort.sorted").isBoolean())
                .andExpect(jsonPath("$.data.sort.unsorted").isBoolean())
                .andExpect(jsonPath("$.data.numberOfElements").isNumber())
                .andExpect(jsonPath("$.data.empty").isBoolean());
    }

    @Test
    @DisplayName("픽픽픽 댓글/답글을 추천한다.")
    void recommendPickComment() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("픽픽픽 댓글"), true, new Count(0), member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentId}/recommends",
                        pick.getId(), pickComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.isRecommended").isBoolean())
                .andExpect(jsonPath("$.data.recommendTotalCount").isNumber());
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, names = {"APPROVAL"}, mode = Mode.EXCLUDE)
    @DisplayName("픽픽픽 댓글을 추천할 때 픽픽픽이 승인상태가 아니면 예외가 발생한다.")
    void recommendPickCommentPickIsNotApproval(ContentStatus contentStatus) throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("픽픽픽 댓글"), true, new Count(0), member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentId}/recommends",
                        pick.getId(), pickComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("회원이 픽픽픽 베스트 댓글을 조회한다.")
    void findPickBestComments() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto1 = createSocialDto("user1", "user1", "김민영", password, "alsdudr97@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto2 = createSocialDto("user2", "user2", "이임하", password, "wlgks555@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto3 = createSocialDto("user3", "user3", "문민주", password, "mmj9908@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto4 = createSocialDto("user4", "user4", "유소영", password, "merooongg@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto5 = createSocialDto("user5", "user5", "장세웅", password, "howisitgoing@kakao.com",
                socialType, role);
        SocialMemberDto socialMemberDto6 = createSocialDto("user6", "user6", "nickname", password, "user6@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto7 = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member1 = Member.createMemberBy(socialMemberDto1);
        Member member2 = Member.createMemberBy(socialMemberDto2);
        Member member3 = Member.createMemberBy(socialMemberDto3);
        Member member4 = Member.createMemberBy(socialMemberDto4);
        Member member5 = Member.createMemberBy(socialMemberDto5);
        Member member6 = Member.createMemberBy(socialMemberDto6);
        Member member7 = Member.createMemberBy(socialMemberDto7);
        memberRepository.saveAll(List.of(member1, member2, member3, member4, member5, member6, member7));

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, new Count(6), member1);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(new Title("픽픽픽 옵션1"), new Count(0), pick,
                PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(new Title("픽픽픽 옵션2"), new Count(0), pick,
                PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 투표 생성
        PickVote member1PickVote = createPickVote(member1, firstPickOption, pick);
        PickVote member2PickVote = createPickVote(member2, firstPickOption, pick);
        PickVote member3PickVote = createPickVote(member3, secondPickOption, pick);
        PickVote member4PickVote = createPickVote(member4, secondPickOption, pick);
        pickVoteRepository.saveAll(List.of(member1PickVote, member2PickVote, member3PickVote, member4PickVote));

        // 픽픽픽 최초 댓글 생성
        PickComment originParentPickComment1 = createPickComment(new CommentContents("여기가 꿈파?"), true, new Count(2),
                new Count(3), member1, pick, member1PickVote);
        originParentPickComment1.modifyCommentContents(new CommentContents("행복한~"), LocalDateTime.now());
        PickComment originParentPickComment2 = createPickComment(new CommentContents("꿈빛!"), true, new Count(1),
                new Count(2), member2, pick, member2PickVote);
        PickComment originParentPickComment3 = createPickComment(new CommentContents("파티시엘~!"), true, new Count(0),
                new Count(1), member3, pick, member3PickVote);
        PickComment originParentPickComment4 = createPickComment(new CommentContents("댓글4"), false, new Count(0),
                new Count(0), member4, pick, member4PickVote);
        PickComment originParentPickComment5 = createPickComment(new CommentContents("댓글5"), false, new Count(0),
                new Count(0), member5, pick, null);
        PickComment originParentPickComment6 = createPickComment(new CommentContents("댓글6"), false, new Count(0),
                new Count(0), member6, pick, null);
        pickCommentRepository.saveAll(
                List.of(originParentPickComment6, originParentPickComment5, originParentPickComment4,
                        originParentPickComment3, originParentPickComment2, originParentPickComment1));

        // 픽픽픽 답글 생성
        PickComment pickReply1 = createReplidPickComment(new CommentContents("진짜 너무 좋아"), member1, pick,
                originParentPickComment1, originParentPickComment1);
        PickComment pickReply2 = createReplidPickComment(new CommentContents("너무 행복하다"), member6, pick,
                originParentPickComment1, pickReply1);
        pickReply2.changeDeletedAt(LocalDateTime.now(), member1);
        PickComment pickReply3 = createReplidPickComment(new CommentContents("사랑해요~"), member6, pick,
                originParentPickComment2, originParentPickComment2);
        pickCommentRepository.saveAll(List.of(pickReply1, pickReply2, pickReply3));

        // 추천 생성
        PickCommentRecommend pickCommentRecommend = createPickCommentRecommend(originParentPickComment1, member1, true);
        pickCommentRecommendRepository.save(pickCommentRecommend);

        em.flush();
        em.clear();

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}/comments/best",
                        pick.getId())
                        .queryParam("size", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
                .andExpect(jsonPath("$.datas").isNotEmpty())
                .andExpect(jsonPath("$.datas").isArray())
                .andExpect(jsonPath("$.datas.[0].pickCommentId").isNumber())
                .andExpect(jsonPath("$.datas.[0].createdAt").isString())
                .andExpect(jsonPath("$.datas.[0].memberId").isNumber())
                .andExpect(jsonPath("$.datas.[0].author").isString())
                .andExpect(jsonPath("$.datas.[0].isCommentOfPickAuthor").isBoolean())
                .andExpect(jsonPath("$.datas.[0].isCommentAuthor").isBoolean())
                .andExpect(jsonPath("$.datas.[0].isRecommended").isBoolean())
                .andExpect(jsonPath("$.datas.[0].maskedEmail").isString())
                .andExpect(jsonPath("$.datas.[0].votedPickOption").isString())
                .andExpect(jsonPath("$.datas.[0].votedPickOptionTitle").isString())
                .andExpect(jsonPath("$.datas.[0].contents").isString())
                .andExpect(jsonPath("$.datas.[0].replyTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[0].recommendTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[0].isModified").isBoolean())
                .andExpect(jsonPath("$.datas.[0].isDeleted").isBoolean())
                .andExpect(jsonPath("$.datas.[0].replies.[0].pickCommentId").isNumber())
                .andExpect(jsonPath("$.datas.[0].replies.[0].memberId").isNumber())
                .andExpect(jsonPath("$.datas.[0].replies.[0].pickParentCommentId").isNumber())
                .andExpect(jsonPath("$.datas.[0].replies.[0].pickOriginParentCommentId").isNumber())
                .andExpect(jsonPath("$.datas.[0].replies.[0].createdAt").isString())
                .andExpect(jsonPath("$.datas.[0].replies.[0].isCommentOfPickAuthor").isBoolean())
                .andExpect(jsonPath("$.datas.[0].replies.[0].isCommentAuthor").isBoolean())
                .andExpect(jsonPath("$.datas.[0].replies.[0].isRecommended").isBoolean())
                .andExpect(jsonPath("$.datas.[0].replies.[0].author").isString())
                .andExpect(jsonPath("$.datas.[0].replies.[0].maskedEmail").isString())
                .andExpect(jsonPath("$.datas.[0].replies.[0].contents").isString())
                .andExpect(jsonPath("$.datas.[0].replies.[0].recommendTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[0].replies.[0].isModified").isBoolean())
                .andExpect(jsonPath("$.datas.[0].replies.[0].isDeleted").isBoolean())
                .andExpect(jsonPath("$.datas.[0].replies.[0].pickParentCommentMemberId").isNumber())
                .andExpect(jsonPath("$.datas.[0].replies.[0].pickParentCommentAuthor").isString());
    }

    @Test
    @DisplayName("익명 회원이 픽픽픽 베스트 댓글을 조회한다.")
    void findPickBestCommentsAnonymous() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto1 = createSocialDto("user1", "user1", "김민영", password, "alsdudr97@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto2 = createSocialDto("user2", "user2", "이임하", password, "wlgks555@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto3 = createSocialDto("user3", "user3", "문민주", password, "mmj9908@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto4 = createSocialDto("user4", "user4", "유소영", password, "merooongg@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto5 = createSocialDto("user5", "user5", "장세웅", password, "howisitgoing@kakao.com",
                socialType, role);
        SocialMemberDto socialMemberDto6 = createSocialDto("user6", "user6", "nickname", password, "user6@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto7 = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member1 = Member.createMemberBy(socialMemberDto1);
        Member member2 = Member.createMemberBy(socialMemberDto2);
        Member member3 = Member.createMemberBy(socialMemberDto3);
        Member member4 = Member.createMemberBy(socialMemberDto4);
        Member member5 = Member.createMemberBy(socialMemberDto5);
        Member member6 = Member.createMemberBy(socialMemberDto6);
        Member member7 = Member.createMemberBy(socialMemberDto7);
        memberRepository.saveAll(List.of(member1, member2, member3, member4, member5, member6, member7));

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, new Count(6), member1);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(new Title("픽픽픽 옵션1"), new Count(0), pick,
                PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(new Title("픽픽픽 옵션2"), new Count(0), pick,
                PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 투표 생성
        PickVote member1PickVote = createPickVote(member1, firstPickOption, pick);
        PickVote member2PickVote = createPickVote(member2, firstPickOption, pick);
        PickVote member3PickVote = createPickVote(member3, secondPickOption, pick);
        PickVote member4PickVote = createPickVote(member4, secondPickOption, pick);
        pickVoteRepository.saveAll(List.of(member1PickVote, member2PickVote, member3PickVote, member4PickVote));

        // 픽픽픽 최초 댓글 생성
        PickComment originParentPickComment1 = createPickComment(new CommentContents("여기가 꿈파?"), true, new Count(2),
                new Count(3), member1, pick, member1PickVote);
        originParentPickComment1.modifyCommentContents(new CommentContents("행복한~"), LocalDateTime.now());
        PickComment originParentPickComment2 = createPickComment(new CommentContents("꿈빛!"), true, new Count(1),
                new Count(2), member2, pick, member2PickVote);
        PickComment originParentPickComment3 = createPickComment(new CommentContents("파티시엘~!"), true, new Count(0),
                new Count(1), member3, pick, member3PickVote);
        PickComment originParentPickComment4 = createPickComment(new CommentContents("댓글4"), false, new Count(0),
                new Count(0), member4, pick, member4PickVote);
        PickComment originParentPickComment5 = createPickComment(new CommentContents("댓글5"), false, new Count(0),
                new Count(0), member5, pick, null);
        PickComment originParentPickComment6 = createPickComment(new CommentContents("댓글6"), false, new Count(0),
                new Count(0), member6, pick, null);
        pickCommentRepository.saveAll(
                List.of(originParentPickComment6, originParentPickComment5, originParentPickComment4,
                        originParentPickComment3, originParentPickComment2, originParentPickComment1));

        // 픽픽픽 답글 생성
        PickComment pickReply1 = createReplidPickComment(new CommentContents("진짜 너무 좋아"), member1, pick,
                originParentPickComment1, originParentPickComment1);
        PickComment pickReply2 = createReplidPickComment(new CommentContents("너무 행복하다"), member6, pick,
                originParentPickComment1, pickReply1);
        pickReply2.changeDeletedAt(LocalDateTime.now(), member1);
        PickComment pickReply3 = createReplidPickComment(new CommentContents("사랑해요~"), member6, pick,
                originParentPickComment2, originParentPickComment2);
        pickCommentRepository.saveAll(List.of(pickReply1, pickReply2, pickReply3));

        // 추천 생성
        PickCommentRecommend pickCommentRecommend = createPickCommentRecommend(originParentPickComment1, member1, true);
        pickCommentRecommendRepository.save(pickCommentRecommend);

        em.flush();
        em.clear();

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}/comments/best",
                        pick.getId())
                        .queryParam("size", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
                .andExpect(jsonPath("$.datas").isNotEmpty())
                .andExpect(jsonPath("$.datas").isArray())
                .andExpect(jsonPath("$.datas.[0].pickCommentId").isNumber())
                .andExpect(jsonPath("$.datas.[0].createdAt").isString())
                .andExpect(jsonPath("$.datas.[0].memberId").isNumber())
                .andExpect(jsonPath("$.datas.[0].author").isString())
                .andExpect(jsonPath("$.datas.[0].isCommentOfPickAuthor").isBoolean())
                .andExpect(jsonPath("$.datas.[0].isCommentAuthor").isBoolean())
                .andExpect(jsonPath("$.datas.[0].isRecommended").isBoolean())
                .andExpect(jsonPath("$.datas.[0].maskedEmail").isString())
                .andExpect(jsonPath("$.datas.[0].votedPickOption").isString())
                .andExpect(jsonPath("$.datas.[0].votedPickOptionTitle").isString())
                .andExpect(jsonPath("$.datas.[0].contents").isString())
                .andExpect(jsonPath("$.datas.[0].replyTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[0].recommendTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[0].isModified").isBoolean())
                .andExpect(jsonPath("$.datas.[0].isDeleted").isBoolean())
                .andExpect(jsonPath("$.datas.[0].replies.[0].pickCommentId").isNumber())
                .andExpect(jsonPath("$.datas.[0].replies.[0].memberId").isNumber())
                .andExpect(jsonPath("$.datas.[0].replies.[0].pickParentCommentId").isNumber())
                .andExpect(jsonPath("$.datas.[0].replies.[0].pickOriginParentCommentId").isNumber())
                .andExpect(jsonPath("$.datas.[0].replies.[0].createdAt").isString())
                .andExpect(jsonPath("$.datas.[0].replies.[0].isCommentOfPickAuthor").isBoolean())
                .andExpect(jsonPath("$.datas.[0].replies.[0].isCommentAuthor").isBoolean())
                .andExpect(jsonPath("$.datas.[0].replies.[0].isRecommended").isBoolean())
                .andExpect(jsonPath("$.datas.[0].replies.[0].author").isString())
                .andExpect(jsonPath("$.datas.[0].replies.[0].maskedEmail").isString())
                .andExpect(jsonPath("$.datas.[0].replies.[0].contents").isString())
                .andExpect(jsonPath("$.datas.[0].replies.[0].recommendTotalCount").isNumber())
                .andExpect(jsonPath("$.datas.[0].replies.[0].isModified").isBoolean())
                .andExpect(jsonPath("$.datas.[0].replies.[0].isDeleted").isBoolean())
                .andExpect(jsonPath("$.datas.[0].replies.[0].pickParentCommentMemberId").isNumber())
                .andExpect(jsonPath("$.datas.[0].replies.[0].pickParentCommentAuthor").isString());
    }

    private PickCommentRecommend createPickCommentRecommend(PickComment pickComment, Member member,
                                                            Boolean recommendedStatus) {
        PickCommentRecommend pickCommentRecommend = PickCommentRecommend.builder()
                .member(member)
                .recommendedStatus(recommendedStatus)
                .build();

        pickCommentRecommend.changePickComment(pickComment);

        return pickCommentRecommend;
    }

    private Pick createPick(Title title, Count viewTotalCount, Count commentTotalCount, Count voteTotalCount,
                            Count poplarScore, ContentStatus contentStatus, Member member) {
        return Pick.builder()
                .title(title)
                .viewTotalCount(viewTotalCount)
                .voteTotalCount(voteTotalCount)
                .commentTotalCount(commentTotalCount)
                .popularScore(poplarScore)
                .contentStatus(contentStatus)
                .member(member)
                .build();
    }

    private PickComment createPickComment(CommentContents contents, Boolean isPublic, Count recommendTotalCount,
                                          Member member, Pick pick) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdBy(member)
                .recommendTotalCount(recommendTotalCount)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private PickOption createPickOption(Title title, Count voteTotalCount, Pick pick, PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .voteTotalCount(voteTotalCount)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private Pick createPick(Title title, ContentStatus contentStatus, Count commentTotalCount, Member member) {
        return Pick.builder()
                .title(title)
                .contentStatus(contentStatus)
                .commentTotalCount(commentTotalCount)
                .member(member)
                .build();
    }

    private PickComment createPickComment(CommentContents contents, Boolean isPublic, Count replyTotalCount,
                                          Count recommendTotalCount, Member member, Pick pick, PickVote pickVote) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdBy(member)
                .replyTotalCount(replyTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .pick(pick)
                .pickVote(pickVote)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private PickComment createReplidPickComment(CommentContents contents, Member member, Pick pick,
                                                PickComment originParent, PickComment parent) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .createdBy(member)
                .pick(pick)
                .originParent(originParent)
                .isPublic(false)
                .parent(parent)
                .recommendTotalCount(new Count(0))
                .replyTotalCount(new Count(0))
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private PickComment createReplidPickComment(CommentContents contents, Boolean isPublic, Member member, Pick pick,
                                                PickComment originParent, PickComment parent) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdBy(member)
                .pick(pick)
                .originParent(originParent)
                .parent(parent)
                .replyTotalCount(new Count(0))
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private PickComment createPickComment(CommentContents contents, Boolean isPublic, Member member, Pick pick) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .replyTotalCount(new Count(0))
                .createdBy(member)
                .pick(pick)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private Pick createPick(Title title, ContentStatus contentStatus, Member member) {
        return Pick.builder()
                .title(title)
                .contentStatus(contentStatus)
                .member(member)
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

    private PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents,
                                        PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private PickOptionImage createPickOptionImage(String name, PickOption pickOption) {
        PickOptionImage pickOptionImage = PickOptionImage.builder()
                .name(name)
                .imageKey("imageKey")
                .imageUrl("imageUrl")
                .build();

        pickOptionImage.changePickOption(pickOption);

        return pickOptionImage;
    }

    private PickVote createPickVote(Member member, PickOption pickOption, Pick pick) {
        PickVote pickVote = PickVote.builder()
                .member(member)
                .pickOption(pickOption)
                .pick(pick)
                .build();

        pickVote.changePick(pick);

        return pickVote;
    }
}