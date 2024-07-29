package com.dreamypatisiel.devdevdev.elastic.domain.document;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Document(indexName = "keywords" + "#{@elasticsearchIndexConfigService.getIndexName()}")
public class ElasticKeyword {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String keyword;

    @Builder
    private ElasticKeyword(String id, String keyword) {
        this.id = id;
        this.keyword = keyword;
    }

    public static ElasticKeyword create(String keyword) {
        return ElasticKeyword.builder()
                .keyword(keyword)
                .build();
    }
}
