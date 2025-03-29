package com.dreamypatisiel.devdevdev.web.controller.notification;

import com.dreamypatisiel.devdevdev.domain.service.notification.NotificationService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationReadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
