package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PickServiceStrategy {

    private final ApplicationContext applicationContext;

    public PickService getPickService() {
        if (AuthenticationMemberUtils.isAnonymous()) {
            return applicationContext.getBean(GuestPickService.class);
        }
        return applicationContext.getBean(MemberPickService.class);
    }

    public PickCommentService pickCommentService() {
        if (AuthenticationMemberUtils.isAnonymous()) {
            return applicationContext.getBean(GuestPickCommentService.class);
        }
        return applicationContext.getBean(MemberPickCommentService.class);
    }
}
