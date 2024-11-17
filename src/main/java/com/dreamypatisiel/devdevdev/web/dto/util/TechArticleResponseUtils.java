package com.dreamypatisiel.devdevdev.web.dto.util;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
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

    public static boolean hasNextPage(List<TechArticleMainResponse> contents, Pageable pageable) {
        return contents.size() >= pageable.getPageSize();
    }
}
