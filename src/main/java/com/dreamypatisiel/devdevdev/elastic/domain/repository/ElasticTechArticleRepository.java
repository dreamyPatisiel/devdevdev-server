package com.dreamypatisiel.devdevdev.elastic.domain.repository;

import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.CrudRepository;

public interface ElasticTechArticleRepository extends ElasticsearchRepository<ElasticTechArticle, String>, CrudRepository<ElasticTechArticle, String> {
}
