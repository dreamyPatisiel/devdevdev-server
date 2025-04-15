package com.dreamypatisiel.devdevdev.web.controller.notification;

import com.dreamypatisiel.devdevdev.domain.service.notification.NotificationService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationPopupResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationReadResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationResponse;
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

     @Operation(summary = "알림 팝업 조회")
     @GetMapping("/notifications/popup")
     public ResponseEntity<BasicResponse<SliceCustom<NotificationPopupResponse>>> getNotificationPopup(
             @PageableDefault(size = 5) Pageable pageable
     ) {
         Authentication authentication = AuthenticationMemberUtils.getAuthentication();
         SliceCustom<NotificationPopupResponse> response = notificationService.getNotificationPopup(pageable, authentication);
         return ResponseEntity.ok(BasicResponse.success(response));
     }

    @Operation(summary = "알림 페이지 조회")
    @GetMapping("/notifications/page")
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
