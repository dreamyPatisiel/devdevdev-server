package com.dreamypatisiel.devdevdev.web.docs.format;

import static org.springframework.restdocs.snippet.Attributes.key;

import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickService;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.restdocs.snippet.Attributes;

public interface ApiDocsFormatGenerator {

    String FORMAT = "format";
    String COMMA = ", ";

    static Attributes.Attribute authenticationType() {
        return key("authentication").value("O");
    }

    static Attributes.Attribute pickOptionImageNameType() {
        return key(FORMAT).value(
                MemberPickService.FIRST_PICK_OPTION_IMAGE + " | " + MemberPickService.SECOND_PICK_OPTION_IMAGE);
    }

    static Attributes.Attribute pickSortType() {
        String pickSortType = Arrays.stream(PickSort.values())
                .map(pickSort -> pickSort.name() + "(" + pickSort.getDescription() + ")")
                .collect(Collectors.joining(COMMA));

        return key(FORMAT).value(pickSortType);
    }

    static Attributes.Attribute techArticleSortType() {
        String techArticleSortType = Arrays.stream(TechArticleSort.values())
                .map(techArticleSort -> techArticleSort.name() + "(" + techArticleSort.getDescription() + ")")
                .collect(Collectors.joining(COMMA));

        return key(FORMAT).value(techArticleSortType);
    }

    static Attributes.Attribute yearMonthDateTimeType() {
        return key(FORMAT).value("yyyy-MM-dd HH:mm:ss");
    }

    static Attributes.Attribute bookmarkSortType() {
        String bookmarkSortType = Arrays.stream(BookmarkSort.values())
                .map(bookmarkSort -> bookmarkSort.name() + "(" + bookmarkSort.getDescription() + ")")
                .collect(Collectors.joining(COMMA));

        return key(FORMAT).value(bookmarkSortType);
    }

    static Attributes.Attribute numberOrNull() {
        return key(FORMAT).value("Number | null");
    }

    static Attributes.Attribute stringOrNull() {
        return key(FORMAT).value("String | null");
    }

    static Attributes.Attribute contentStatusType() {
        String contentStatusType = Arrays.stream(ContentStatus.values())
                .map(contentStatus -> contentStatus.name() + "(" + contentStatus.getDescription() + ")")
                .collect(Collectors.joining(COMMA));

        return key(FORMAT).value(contentStatusType);
    }

    static Attributes.Attribute pickCommentSortType() {
        String pickCommentSortType = Arrays.stream(PickCommentSort.values())
                .map(sort -> sort.name() + "(" + sort.getDescription() + ")")
                .collect(Collectors.joining(COMMA));

        return key(FORMAT).value(pickCommentSortType);
    }


    static Attributes.Attribute techCommentSortType() {
        String techCommentSortType = Arrays.stream(TechCommentSort.values())
                .map(sort -> sort.name() + "(" + sort.getDescription() + ")")
                .collect(Collectors.joining(COMMA));

        return key(FORMAT).value(techCommentSortType);
    }

    static Attributes.Attribute pickOptionType() {
        String pickOptionTypeType = Arrays.stream(PickOptionType.values())
                .map(sort -> sort.name() + "(" + sort.getDescription() + ")")
                .collect(Collectors.joining(COMMA));

        return key(FORMAT).value(pickOptionTypeType + " | null");
    }

    static Attributes.Attribute blamePathType() {
        String blamePathType = Arrays.stream(BlamePathType.values())
                .map(sort -> sort.name() + "(" + sort.getDescription() + ")")
                .collect(Collectors.joining(COMMA));

        return key(FORMAT).value(blamePathType);
    }
}
