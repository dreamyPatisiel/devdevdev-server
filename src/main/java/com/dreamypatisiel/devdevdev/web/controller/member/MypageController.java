package com.dreamypatisiel.devdevdev.web.controller.member;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;
import com.dreamypatisiel.devdevdev.domain.service.member.MemberService;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.comment.MyWrittenCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.member.RecordMemberExitSurveyAnswerRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.comment.MyWrittenCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.member.MemberExitSurveyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.MyPickMainResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "내정보 API", description = "내정보 API")
@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
public class MypageController {

    private final MemberService memberService;

    @Operation(summary = "북마크 목록 조회")
    @GetMapping("/mypage/bookmarks")
    public ResponseEntity<BasicResponse<Slice<TechArticleMainResponse>>> getBookmarkedTechArticles(
            @PageableDefault Pageable pageable,
            @RequestParam(required = false) BookmarkSort bookmarkSort, // 등록순, 최신순, 조회순
            @RequestParam(required = false) Long techArticleId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        Slice<TechArticleMainResponse> response = memberService.getBookmarkedTechArticles(pageable, techArticleId,
                bookmarkSort, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/mypage/profile")
    public ResponseEntity<BasicResponse<Void>> deleteMember(HttpServletResponse response) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        memberService.deleteMember(authentication);

        // 쿠키 설정
        CookieUtils.addCookieToResponse(response, JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN,
                CookieUtils.BLANK, CookieUtils.DEFAULT_MAX_AGE, false, true);
        CookieUtils.addCookieToResponse(response, JwtCookieConstant.DEVDEVDEV_LOGIN_STATUS,
                CookieUtils.INACTIVE, CookieUtils.DEFAULT_MAX_AGE, false, true);

        return ResponseEntity.ok(BasicResponse.success());
    }

    @Operation(summary = "내가 작성한 픽픽픽 조회", description = "본인이 작성한 픽픽픽을 커서 방식으로 조회합니다.")
    @GetMapping("/mypage/picks")
    public ResponseEntity<BasicResponse<Slice<MyPickMainResponse>>> getMyPicksMain(
            @PageableDefault(sort = "id", direction = Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Long pickId) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        Slice<MyPickMainResponse> response = memberService.findMyPickMain(pageable, pickId, authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "DEVDEVDEV 회원 탈퇴 서베이 조회", description = "DEVDEVDEV 회원 탈퇴 서베이를 조회합니다.")
    @GetMapping("/mypage/exit-survey")
    public ResponseEntity<BasicResponse<MemberExitSurveyResponse>> getMemberExitSurvey() {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        MemberExitSurveyResponse response = memberService.findMemberExitSurvey(authentication);

        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "DEVDEVDEV 회원 탈퇴 서베이 이력 저장", description = "DEVDEVDEV 회원 탈퇴 서베이 이력을 저장합니다.")
    @PostMapping("/mypage/exit-survey")
    public ResponseEntity<BasicResponse<Void>> recordMemberExitSurvey(@RequestBody @Validated
                                                                      RecordMemberExitSurveyAnswerRequest recordMemberExitSurveyAnswerRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        memberService.recordMemberExitSurveyAnswer(recordMemberExitSurveyAnswerRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success());
    }

    @Operation(summary = "내가 작성한 댓글 조회", description = "본인이 작성한 댓글을 무한 스크롤 방식으로 조회합니다.")
    @GetMapping("/mypage/comments")
    public ResponseEntity<BasicResponse<SliceCustom<MyWrittenCommentResponse>>> getMyWrittenComments(
            @PageableDefault(size = 6) Pageable pageable,
            @Valid @ModelAttribute MyWrittenCommentRequest myWrittenCommentRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();

        SliceCustom<MyWrittenCommentResponse> myWrittenComments = memberService.findMyWrittenComments(pageable,
                myWrittenCommentRequest, authentication);

        return ResponseEntity.ok(BasicResponse.success(myWrittenComments));
    }
}
