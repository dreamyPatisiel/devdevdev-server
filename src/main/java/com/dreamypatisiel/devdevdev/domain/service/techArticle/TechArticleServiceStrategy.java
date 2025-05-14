package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription.GuestSubscriptionService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription.MemberSubscriptionService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription.SubscriptionService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.GuestTechArticleService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.MemberTechArticleService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.TechArticleService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techComment.GuestTechCommentService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techComment.MemberTechCommentService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techComment.TechCommentService;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TechArticleServiceStrategy {

    private final ApplicationContext applicationContext;

    public TechArticleService getTechArticleService() {
        if (AuthenticationMemberUtils.isAnonymous()) {
            return applicationContext.getBean(GuestTechArticleService.class);
        }
        return applicationContext.getBean(MemberTechArticleService.class);
    }

    public TechCommentService getTechCommentService() {
        if (AuthenticationMemberUtils.isAnonymous()) {
            return applicationContext.getBean(GuestTechCommentService.class);
        }
        return applicationContext.getBean(MemberTechCommentService.class);
    }

    public SubscriptionService getSubscriptionService() {
        if (AuthenticationMemberUtils.isAnonymous()) {
            return applicationContext.getBean(GuestSubscriptionService.class);
        }
        return applicationContext.getBean(MemberSubscriptionService.class);
    }
}
