package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.TechCommentRecommend;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;

public class TechTestUtils {

    public static TechCommentRecommend createTechCommentRecommend(Boolean recommendedStatus, TechComment techComment,
                                                                  Member member) {
        TechCommentRecommend techCommentRecommend = TechCommentRecommend.builder()
                .recommendedStatus(recommendedStatus)
                .techComment(techComment)
                .member(member)
                .build();

        techCommentRecommend.changeTechComment(techComment);

        return techCommentRecommend;
    }

    public static TechComment createMainTechComment(CommentContents contents, Member createdBy, TechArticle techArticle,
                                                    Count blameTotalCount, Count recommendTotalCount, Count replyTotalCount) {
        return TechComment.builder()
                .contents(contents)
                .createdBy(createdBy)
                .techArticle(techArticle)
                .blameTotalCount(blameTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .replyTotalCount(replyTotalCount)
                .build();
    }

    public static TechComment createMainTechComment(CommentContents contents, AnonymousMember createdAnonymousBy,
                                                    TechArticle techArticle, Count blameTotalCount, Count recommendTotalCount,
                                                    Count replyTotalCount) {
        return TechComment.builder()
                .contents(contents)
                .createdAnonymousBy(createdAnonymousBy)
                .techArticle(techArticle)
                .blameTotalCount(blameTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .replyTotalCount(replyTotalCount)
                .build();
    }

    public static TechComment createRepliedTechComment(CommentContents contents, Member createdBy,
                                                       TechArticle techArticle,
                                                       TechComment originParent, TechComment parent,
                                                       Count blameTotalCount, Count recommendTotalCount,
                                                       Count replyTotalCount) {
        return TechComment.builder()
                .contents(contents)
                .createdBy(createdBy)
                .techArticle(techArticle)
                .blameTotalCount(blameTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .replyTotalCount(replyTotalCount)
                .originParent(originParent)
                .parent(parent)
                .build();
    }

    public static TechComment createRepliedTechComment(CommentContents contents, AnonymousMember createdAnonymousBy,
                                                       TechArticle techArticle, TechComment originParent, TechComment parent,
                                                       Count blameTotalCount, Count recommendTotalCount,
                                                       Count replyTotalCount) {
        return TechComment.builder()
                .contents(contents)
                .createdAnonymousBy(createdAnonymousBy)
                .techArticle(techArticle)
                .blameTotalCount(blameTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .replyTotalCount(replyTotalCount)
                .originParent(originParent)
                .parent(parent)
                .build();
    }

    public static SocialMemberDto createSocialDto(String userId, String name, String nickName, String password, String email,
                                                  String socialType, String role) {
        return SocialMemberDto.builder()
                .userId(userId)
                .name(name)
                .nickname(nickName)
                .password(password)
                .email(email)
                .socialType(SocialType.valueOf(socialType))
                .role(Role.valueOf(role))
                .build();
    }

    public static Company createCompany(String companyName, String officialImageUrl, String officialUrl,
                                        String careerUrl) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .officialUrl(new Url(officialUrl))
                .careerUrl(new Url(careerUrl))
                .officialImageUrl(new Url(officialImageUrl))
                .build();
    }
}
