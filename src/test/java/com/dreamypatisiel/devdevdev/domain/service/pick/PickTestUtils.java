package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.PickCommentRecommend;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickRequest;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

public abstract class PickTestUtils {

    public static Pick createPick(Title title, Count pickVoteCount, Count commentTotalCount, Member member,
                                  ContentStatus contentStatus, List<Double> embeddings) {
        return Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteCount)
                .commentTotalCount(commentTotalCount)
                .member(member)
                .contentStatus(contentStatus)
                .embeddings(embeddings)
                .build();
    }

    public static Pick createPick(Title title, ContentStatus contentStatus, Count viewTotalCount, Count voteTotalCount,
                                  Count commentTotalCount, Count popularScore, Member member) {
        return Pick.builder()
                .title(title)
                .contentStatus(contentStatus)
                .viewTotalCount(viewTotalCount)
                .voteTotalCount(voteTotalCount)
                .commentTotalCount(commentTotalCount)
                .popularScore(popularScore)
                .member(member)
                .build();
    }

    public static PickComment createPickComment(CommentContents contents, Boolean isPublic, Count recommendTotalCount,
                                                Member member, Pick pick) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdBy(member)
                .recommendTotalCount(recommendTotalCount)
                .pick(pick)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    public static PickCommentRecommend createPickCommentRecommend(PickComment pickComment, Member member,
                                                                  Boolean recommendedStatus) {
        PickCommentRecommend pickCommentRecommend = PickCommentRecommend.builder()
                .member(member)
                .recommendedStatus(recommendedStatus)
                .build();

        pickCommentRecommend.changePickComment(pickComment);

        return pickCommentRecommend;
    }

    public static Pick createPick(Title title, ContentStatus contentStatus, Count commentTotalCount, Member member) {
        return Pick.builder()
                .title(title)
                .contentStatus(contentStatus)
                .commentTotalCount(commentTotalCount)
                .member(member)
                .build();
    }

    public static PickComment createPickComment(CommentContents contents, Boolean isPublic, Count replyTotalCount,
                                                Count recommendTotalCount, Member member, Pick pick, PickVote pickVote) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdBy(member)
                .replyTotalCount(replyTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .pick(pick)
                .pickVote(pickVote)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    public static PickComment createPickComment(CommentContents contents, Boolean isPublic, Count replyTotalCount,
                                                Count recommendTotalCount, AnonymousMember anonymousMember, Pick pick,
                                                PickVote pickVote) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdAnonymousBy(anonymousMember)
                .replyTotalCount(replyTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .pick(pick)
                .pickVote(pickVote)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    public static PickComment createReplidPickComment(CommentContents contents, Member member, Pick pick,
                                                      PickComment originParent, PickComment parent) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .createdBy(member)
                .pick(pick)
                .originParent(originParent)
                .isPublic(false)
                .parent(parent)
                .recommendTotalCount(new Count(0))
                .replyTotalCount(new Count(0))
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    public static PickComment createReplidPickComment(CommentContents contents, AnonymousMember anonymousMember, Pick pick,
                                                      PickComment originParent, PickComment parent) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .createdAnonymousBy(anonymousMember)
                .pick(pick)
                .originParent(originParent)
                .isPublic(false)
                .parent(parent)
                .recommendTotalCount(new Count(0))
                .replyTotalCount(new Count(0))
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    public static PickComment createPickComment(CommentContents contents, Boolean isPublic, Member member, Pick pick) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdBy(member)
                .replyTotalCount(new Count(0))
                .pick(pick)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    public static PickComment createPickComment(CommentContents contents, Boolean isPublic, AnonymousMember anonymousMember,
                                                Pick pick) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdAnonymousBy(anonymousMember)
                .replyTotalCount(new Count(0))
                .pick(pick)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    public static Pick createPick(Title title, ContentStatus contentStatus, Member member) {
        return Pick.builder()
                .title(title)
                .contentStatus(contentStatus)
                .member(member)
                .build();
    }

    public static PickOption createPickOption(Title title, Count voteTotalCount, Pick pick, PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .voteTotalCount(voteTotalCount)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    public static Pick createPick(Title title, Count viewTotalCount, Count commentTotalCount, Count voteTotalCount,
                                  Count poplarScore, Member member, ContentStatus contentStatus) {
        return Pick.builder()
                .title(title)
                .viewTotalCount(viewTotalCount)
                .voteTotalCount(voteTotalCount)
                .commentTotalCount(commentTotalCount)
                .popularScore(poplarScore)
                .member(member)
                .contentStatus(contentStatus)
                .build();
    }

    public static ModifyPickRequest createModifyPickRequest(String pickTitle,
                                                            Map<PickOptionType, ModifyPickOptionRequest> modifyPickOptionRequests) {
        return ModifyPickRequest.builder()
                .pickTitle(pickTitle)
                .pickOptions(modifyPickOptionRequests)
                .build();
    }

    public static PickOptionImage createPickOptionImage(String name, String imageUrl, String imageKey) {
        return PickOptionImage.builder()
                .name(name)
                .imageUrl(imageUrl)
                .imageKey(imageKey)
                .build();
    }

    public static PickOptionImage createPickOptionImage(String name) {
        return PickOptionImage.builder()
                .name(name)
                .imageUrl("imageUrl")
                .imageKey("imageKey")
                .build();
    }

    public static PickOptionImage createPickOptionImage(String name, String imageUrl, PickOption pickOption) {
        PickOptionImage pickOptionImage = PickOptionImage.builder()
                .name(name)
                .imageUrl(imageUrl)
                .imageKey("imageKey")
                .build();

        pickOptionImage.changePickOption(pickOption);

        return pickOptionImage;
    }

    public static PickOptionImage createPickOptionImage(String name, PickOption pickOption) {
        PickOptionImage pickOptionImage = PickOptionImage.builder()
                .name(name)
                .imageUrl("imageUrl")
                .imageKey("imageKey")
                .build();

        pickOptionImage.changePickOption(pickOption);

        return pickOptionImage;
    }

    public static RegisterPickRequest createPickRegisterRequest(String pickTitle,
                                                                Map<PickOptionType, RegisterPickOptionRequest> pickOptions) {
        return RegisterPickRequest.builder()
                .pickTitle(pickTitle)
                .pickOptions(pickOptions)
                .build();
    }

    public static RegisterPickOptionRequest createPickOptionRequest(String pickOptionTitle, String pickOptionContent,
                                                                    List<Long> pickOptionImageIds) {
        return RegisterPickOptionRequest.builder()
                .pickOptionTitle(pickOptionTitle)
                .pickOptionContent(pickOptionContent)
                .pickOptionImageIds(pickOptionImageIds)
                .build();
    }

    public static MockMultipartFile createMockMultipartFile(String name, String originalFilename) {
        return new MockMultipartFile(
                name,
                originalFilename,
                MediaType.IMAGE_PNG_VALUE,
                name.getBytes()
        );
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

    public static Pick createPick(Title title, Member member) {
        return Pick.builder()
                .title(title)
                .member(member)
                .build();
    }

    public static Pick createPick(Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                                  Count pickcommentTotalCount, Count pickPopularScore, String thumbnailUrl,
                                  String author, ContentStatus contentStatus
    ) {

        return Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteTotalCount)
                .viewTotalCount(pickViewTotalCount)
                .commentTotalCount(pickcommentTotalCount)
                .popularScore(pickPopularScore)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .contentStatus(contentStatus)
                .build();
    }

    public static Pick createPick(Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                                  Count pickcommentTotalCount, String thumbnailUrl, String author,
                                  ContentStatus contentStatus,
                                  List<PickVote> pickVotes
    ) {

        Pick pick = Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteTotalCount)
                .viewTotalCount(pickViewTotalCount)
                .commentTotalCount(pickcommentTotalCount)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .contentStatus(contentStatus)
                .build();

        pick.changePickVote(pickVotes);

        return pick;
    }

    public static PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents,
                                              Count voteTotalCount, PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(voteTotalCount)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    public static PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents,
                                              PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .pickOptionType(pickOptionType)
                .contents(pickOptionContents)
                .pick(pick)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    public static PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents,
                                              Count pickOptionVoteCount) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(pickOptionVoteCount)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    public static PickOption createPickOption(Title title, PickOptionContents pickOptionContents,
                                              PickOptionType pickOptionType) {
        return PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .pickOptionType(pickOptionType)
                .build();
    }

    public static PickVote createPickVote(Member member, PickOption pickOption, Pick pick) {
        PickVote pickVote = PickVote.builder()
                .member(member)
                .build();

        pickVote.changePickOption(pickOption);
        pickVote.changePick(pick);

        return pickVote;
    }

    public static PickVote createPickVote(AnonymousMember anonymousMember, PickOption pickOption, Pick pick) {
        PickVote pickVote = PickVote.builder()
                .anonymousMember(anonymousMember)
                .build();

        pickVote.changePickOption(pickOption);
        pickVote.changePick(pick);

        return pickVote;
    }
}
