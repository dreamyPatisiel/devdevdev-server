package com.dreamypatisiel.devdevdev.web.controller.notification;

import com.dreamypatisiel.devdevdev.domain.service.notification.NotificationService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationPopupResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationReadResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "알림 API", description = "알림 API")
@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "알림 단건 읽음 처리")
    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<BasicResponse<NotificationReadResponse>> readNotification(
            @PathVariable Long notificationId
    ) {
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        NotificationReadResponse response = notificationService.readNotification(notificationId, authentication);
        return ResponseEntity.ok(BasicResponse.success(response));
    }

    @Operation(summary = "모든 알림 읽음 처리")
    @PatchMapping("/notifications/read-all")
    public ResponseEntity<BasicResponse<Void>> readAllNotifications() {
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        notificationService.readAllNotifications(authentication);
        return ResponseEntity.ok(BasicResponse.success());
    }

    /**
     * 알림 팝업 조회(최근 알림 5개 조회)
     * - 최근 5개로 고정이지만 추후 정책 변경 대응을 위해 Pageable 사용
     * - 알림 팝업은 읽음 여부 상관하지 않음
     * - 읽지 않은 알림 총 개수 전달
     * - input: 유저 정보, pageable
     * - output: 알림 팝업 리스트
     */
     @Operation(summary = "알림 팝업 조회")
     @GetMapping("/notifications/popup")
     public ResponseEntity<BasicResponse<SliceCustom<NotificationPopupResponse>>> getNotificationPopup(
             @PageableDefault(size = 5) Pageable pageable
     ) {
         Authentication authentication = AuthenticationMemberUtils.getAuthentication();
         SliceCustom<NotificationPopupResponse> response = notificationService.getNotificationPopup(pageable, authentication);
         return ResponseEntity.ok(BasicResponse.success(response));
     }

    /**
     * 알림 페이지 조회(전체 알림 조회)
     * - notificationId 무한스크롤 조회, 정렬 옵션 X (최신순)
     * - 읽음 여부 상관하지 않고 모두 조회
     * - 읽지 않은 알림 총 개수 전달
     */
    @Operation(summary = "알림 페이지 조회")
    @GetMapping("/notifications")
    public ResponseEntity<BasicResponse<SliceCustom<NotificationResponse>>> getNotifications(
            @PageableDefault Pageable pageable,
            @RequestParam(required = false) Long notificationId
    ) {
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        SliceCustom<NotificationResponse> response = notificationService.getNotifications(pageable, notificationId,
                authentication);
        return ResponseEntity.ok(BasicResponse.success(response));
    }
}
