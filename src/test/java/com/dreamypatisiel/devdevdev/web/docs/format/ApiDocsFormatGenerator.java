package com.dreamypatisiel.devdevdev.web.docs.format;

import static org.springframework.restdocs.snippet.Attributes.key;

import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickService;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.restdocs.snippet.Attributes;

public interface ApiDocsFormatGenerator {

    String FORMAT = "format";

    static Attributes.Attribute authenticationType() {
        return key("authentication").value("O");
    }

    static Attributes.Attribute pickOptionImageNameType() {
        return key(FORMAT).value(MemberPickService.FIRST_PICK_OPTION_IMAGE +" | "+ MemberPickService.SECOND_PICK_OPTION_IMAGE);
    }

    static Attributes.Attribute pickSortType() {
        String pickSortType = Arrays.stream(PickSort.values())
                .map(pickSort -> pickSort.name()+"("+pickSort.getDescription()+")")
                .collect(Collectors.joining(", "));

        return key(FORMAT).value(pickSortType);
    }

    static Attributes.Attribute techArticleSortType() {
        String techArticleSortType = Arrays.stream(TechArticleSort.values())
                .map(techArticleSort -> techArticleSort.name()+"("+techArticleSort.getDescription()+")")
                .collect(Collectors.joining(", "));

        return key(FORMAT).value(techArticleSortType);
    }
}
