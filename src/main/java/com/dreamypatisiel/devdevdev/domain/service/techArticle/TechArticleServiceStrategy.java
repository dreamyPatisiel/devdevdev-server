package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TechArticleServiceStrategy {

    private final ApplicationContext applicationContext;

    public TechArticleService getTechArticleService() {
        if(AuthenticationMemberUtils.isAnonymous()) {
            return applicationContext.getBean(GuestTechArticleService.class);
        }
        return applicationContext.getBean(MemberTechArticleService.class);
    }
}
