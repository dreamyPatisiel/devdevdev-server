package com.dreamypatisiel.devdevdev.elastic.domain.document;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;

@Getter
@Document(indexName = "keywords" + "#{@elasticsearchIndexConfigService.getIndexName()}")
public class ElasticKeyword {
    @Id
    private String id;

    @CompletionField
    private List<String> words;

    @Builder
    private ElasticKeyword(String id, List<String> words) {
        this.id = id;
        this.words = words;
    }

    public static ElasticKeyword create(List<String> words) {
        return ElasticKeyword.builder()
                .words(words)
                .build();
    }
}
