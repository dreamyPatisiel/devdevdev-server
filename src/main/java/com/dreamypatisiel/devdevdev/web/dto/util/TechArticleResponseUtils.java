package com.dreamypatisiel.devdevdev.web.dto.util;

import com.dreamypatisiel.devdevdev.domain.entity.*;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public class TechArticleResponseUtils {

    public static boolean isBookmarkedByMember(TechArticle techArticle, Member member) {
        Optional<Bookmark> bookmarks = techArticle.getBookmarks().stream()
                .filter(bookmark -> bookmark.getMember().isEqualsId(member.getId()))
                .findAny();

        return bookmarks.map(Bookmark::isBookmarked).orElse(false);
    }

    public static boolean isRecommendedByMember(TechArticle techArticle, Member member) {
        Optional<TechArticleRecommend> recommends = techArticle.getRecommends().stream()
                .filter(recommend -> recommend.getMember().isEqualsId(member.getId()))
                .findAny();

        return recommends.map(TechArticleRecommend::isRecommended).orElse(false);
    }

    public static boolean isRecommendedByAnonymousMember(TechArticle techArticle, AnonymousMember anonymousMember) {
        Optional<TechArticleRecommend> recommends = techArticle.getRecommends().stream()
                .filter(recommend -> recommend.getAnonymousMember().isEqualAnonymousMemberId(anonymousMember.getId()))
                .findAny();

        return recommends.map(TechArticleRecommend::isRecommended).orElse(false);
    }

    public static boolean hasNextPage(List<TechArticleMainResponse> contents, Pageable pageable) {
        return contents.size() >= pageable.getPageSize();
    }
}
