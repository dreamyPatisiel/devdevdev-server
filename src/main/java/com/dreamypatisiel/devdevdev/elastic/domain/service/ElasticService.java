//package com.dreamypatisiel.devdevdev.elastic.domain.service;
//
//import com.dreamypatisiel.devdevdev.elastic.data.response.ElasticResponse;
//import java.util.List;
//import org.springframework.data.elasticsearch.core.SearchHits;
//
//public interface ElasticService<T> {
//    default List<ElasticResponse<T>> mapToElasticResponse(SearchHits<T> searchHits) {
//        return searchHits.stream()
//                .map(searchHit -> new ElasticResponse<>(searchHit.getContent(), searchHit.getScore()))
//                .toList();
//    }
//}
