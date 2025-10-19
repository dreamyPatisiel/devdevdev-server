package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.controller.ApiVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PickServiceStrategy {

    private final ApplicationContext applicationContext;

    public PickService getPickService(ApiVersion apiVersion) {
        return switch (apiVersion) {
            case V1 -> AuthenticationMemberUtils.isAnonymous()
                    ? applicationContext.getBean(GuestPickService.class)
                    : applicationContext.getBean(MemberPickService.class);
            case V2 -> AuthenticationMemberUtils.isAnonymous()
                    ? applicationContext.getBean(GuestPickServiceV2.class)
                    : applicationContext.getBean(MemberPickServiceV2.class);
        };
    }

    public PickCommentService pickCommentService() {
        if (AuthenticationMemberUtils.isAnonymous()) {
            return applicationContext.getBean(GuestPickCommentServiceV2.class);
        }
        return applicationContext.getBean(MemberPickCommentService.class);
    }
}
