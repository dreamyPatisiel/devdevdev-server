package com.dreamypatisiel.devdevdev.web.controller.notification;

import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.domain.service.notification.NotificationService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticleRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationReadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "알림 API", description = "알림 관련 API")
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

    @Operation(summary = "실시간 알림 수신 활성화", description = "실시간 알림 수신을 활성화 합니다.")
    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter notifications() {
        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        return notificationService.addClientAndSendNotification(authentication);
    }

    @Operation(summary = "알림 생성", description = "알림을 생성 합니다.")
    @PostMapping("/notifications/{channel}")
    public ResponseEntity<BasicResponse<Void>> publish(
            @PathVariable NotificationType channel,
            @RequestBody @Validated PublishTechArticleRequest publishTechArticleRequest) {

        Authentication authentication = AuthenticationMemberUtils.getAuthentication();
        notificationService.publish(authentication, channel, publishTechArticleRequest);

        return ResponseEntity.ok(BasicResponse.success());
    }
}
