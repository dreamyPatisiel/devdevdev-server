package com.dreamypatisiel.devdevdev.elastic.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
public class ElasticsearchIndexConfigService {
    @Value("${elasticsearch.index:}")
    private String indexName;
}