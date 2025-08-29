//package com.dreamypatisiel.devdevdev.elastic.domain.service;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.opensearch.action.search.SearchRequest;
//import org.opensearch.action.search.SearchResponse;
//import org.opensearch.client.RequestOptions;
//import org.opensearch.client.RestHighLevelClient;
//import org.opensearch.common.unit.Fuzziness;
//import org.opensearch.index.query.MultiMatchQueryBuilder;
//import org.opensearch.index.query.QueryBuilders;
//import org.opensearch.search.builder.SearchSourceBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class ElasticKeywordService {
//
//    @Value("#{@elasticsearchIndexConfigService.getIndexName()}")
//    private String INDEX_NAME_POSTFIX;
//    public static final String INDEX_NAME = "keywords";
//    public static final String FIELD_NAME = "keyword";
//    public static final String[] MULTI_FIELD_NAMES = {"keyword", "keyword.nfc", "keyword.chosung"};
//    public static final int AUTOCOMPLETION_MAX_SIZE = 20;
//
//
//    private final RestHighLevelClient elasticsearchClient;
//
//    public List<String> autocompleteKeyword(String prefix) throws IOException {
//
//        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(
//                prefix, // 검색할 쿼리 스트링
//                MULTI_FIELD_NAMES); // multi-match 쿼리를 실행할 필드 목록 정의
//        multiMatchQueryBuilder.fuzziness(Fuzziness.ZERO); // Fuzziness를 0으로 설정하여 정확히 일치하는 키워드만 검색
//
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
//                .query(multiMatchQueryBuilder)
//                .size(AUTOCOMPLETION_MAX_SIZE); // 최대 20개 조회
//
//        // 조회 쿼리 생성
//        SearchRequest searchRequest = new SearchRequest(INDEX_NAME + INDEX_NAME_POSTFIX)
//                .source(searchSourceBuilder);
//
//        // 응답 데이터 가공
//        SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
//        return Arrays.stream(searchResponse.getHits().getHits())
//                .map(hit -> hit.getSourceAsMap().get(FIELD_NAME).toString())
//                .toList();
//    }
//}
//
