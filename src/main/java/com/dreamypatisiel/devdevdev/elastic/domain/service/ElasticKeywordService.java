package com.dreamypatisiel.devdevdev.elastic.domain.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticKeywordService {

    @Value("#{@elasticsearchIndexConfigService.getIndexName()}")
    private String INDEX_NAME_POSTFIX;
    public static final String INDEX_NAME = "keywords";
    public static final String SUGGEST_FIELD_NAME = "words";
    public static final String SUGGESTION_KEY = "suggest";
    public static final int AUTOCOMPLETION_MAX_SIZE = 20;


    private final RestHighLevelClient elasticsearchClient;

    public List<String> autocompleteKeyword(String prefix) throws IOException {

        System.out.println("hello " + INDEX_NAME + INDEX_NAME_POSTFIX);

        // suggest 쿼리 생성
        CompletionSuggestionBuilder completionSuggestionBuilder = SuggestBuilders
                .completionSuggestion(SUGGEST_FIELD_NAME)
                .prefix(prefix, Fuzziness.ZERO) // Fuzziness를 0으로 설정하여 정확히 일치하는 키워드만 검색
                .size(AUTOCOMPLETION_MAX_SIZE); // 최대 20개 조회

        SuggestBuilder suggestBuilder = new SuggestBuilder()
                .addSuggestion(SUGGESTION_KEY, completionSuggestionBuilder);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .suggest(suggestBuilder);

        SearchRequest searchRequest = new SearchRequest(INDEX_NAME + INDEX_NAME_POSTFIX)
                .source(searchSourceBuilder);

        // 응답 데이터 가공
        SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse.getSuggest().getSuggestion(SUGGESTION_KEY).getEntries().stream()
                .flatMap(entry -> entry.getOptions().stream())
                .map(option -> option.getText().string())
                .collect(Collectors.toList());
    }
}

