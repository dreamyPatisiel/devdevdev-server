package com.dreamypatisiel.devdevdev.elastic.domain.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

@Getter
@Setter
@Document(indexName = "articles")
public class ElasticTechArticle {
    @Id
    private String id;
    @Field(type = FieldType.Text)
    private String title;
    @Field(type = FieldType.Date)
    private LocalDate regDate;
    @Field(type = FieldType.Text)
    private String contents;
    @Field(type = FieldType.Text)
    private String techArticleUrl;
    @Field(type = FieldType.Text)
    private String description;
    @Field(type = FieldType.Text)
    private String thumbnailUrl;
    @Field(type = FieldType.Text)
    private String author;
    @Field(type = FieldType.Text)
    private String company;
    @Field(type = FieldType.Long)
    private Long viewTotalCount;
    @Field(type = FieldType.Long)
    private Long recommendTotalCount;
    @Field(type = FieldType.Long)
    private Long commentTotalCount;
    @Field(type = FieldType.Long)
    private Long popularScore;

    @Builder
    public ElasticTechArticle(String id, String title, LocalDate regDate, String contents, String techArticleUrl,
                              String description, String thumbnailUrl, String author, String company,
                              Long viewTotalCount, Long recommendTotalCount, Long commentTotalCount, Long popularScore) {
        this.id = id;
        this.title = title;
        this.regDate = regDate;
        this.contents = contents;
        this.techArticleUrl = techArticleUrl;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.author = author;
        this.company = company;
        this.viewTotalCount = viewTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.commentTotalCount = commentTotalCount;
        this.popularScore = popularScore;
    }

    public static ElasticTechArticle of(String title, LocalDate regDate, String contents, String techArticleUrl,
                                        Long viewTotalCount, Long recommendTotalCount, Long commentTotalCount, Long popularScore) {
        return ElasticTechArticle.builder()
                .title(title)
                .regDate(regDate)
                .contents(contents)
                .techArticleUrl(techArticleUrl)
                .viewTotalCount(viewTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .commentTotalCount(commentTotalCount)
                .popularScore(popularScore)
                .build();
    }
}
