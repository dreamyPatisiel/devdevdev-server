package com.dreamypatisiel.devdevdev.domain.service.response.util;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleMainResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public class TechArticleResponseUtils {

    public static boolean isBookmarkedByMember(TechArticle techArticle, Member member) {
        Optional<Bookmark> bookmarks = techArticle.getBookmarks().stream()
                .filter(bookmark -> bookmark.getMember().isEqualMember(member))
                .findAny();

        return bookmarks.map(Bookmark::isBookmarked).orElse(false);
    }

    public static boolean hasNextPage(List<TechArticleMainResponse> contents, Pageable pageable) {
        return contents.size() >= pageable.getPageSize();
    }
}
