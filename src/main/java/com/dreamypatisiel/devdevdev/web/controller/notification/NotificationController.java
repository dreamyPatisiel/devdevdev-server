package com.dreamypatisiel.devdevdev.web.controller.notification;

import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.domain.service.ApiKeyService;
import com.dreamypatisiel.devdevdev.domain.service.notification.NotificationService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;

import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;

import com.dreamypatisiel.devdevdev.global.validator.ValidEnum;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticleRequest;

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

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Tag(name = "알림 API", description = "알림 관련 API")
@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final ApiKeyService apiKeyService;

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

    @Operation(summary = "알림 전체 개수 조회")
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<BasicResponse<Long>> getUnreadNotificationCount() {
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        Long response = notificationService.getUnreadNotificationCount(authentication);
        return ResponseEntity.ok(BasicResponse.success(response));
    }
  
    @Operation(summary = "실시간 알림 수신 활성화", description = "실시간 알림 수신을 활성화 합니다.")
    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter notifications() {
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        return notificationService.addClientAndSendNotification(authentication);
    }

    @Operation(summary = "알림 생성", description = "알림을 생성 합니다.")
    @PostMapping("/notifications/{channel}")
    public ResponseEntity<BasicResponse<Void>> publish(
            @PathVariable @ValidEnum(enumClass = NotificationType.class) String channel,
            @RequestBody @Validated PublishTechArticleRequest publishTechArticleRequest,
            @RequestHeader(name = "service-name") String serviceName,
            @RequestHeader(name = "api-key") String apiKey) {

        // API Key 검증
        apiKeyService.validateApiKey(serviceName, apiKey);

        // 알림 발행
        notificationService.publish(NotificationType.valueOf(channel), publishTechArticleRequest);

        return ResponseEntity.ok(BasicResponse.success());
    }
}
