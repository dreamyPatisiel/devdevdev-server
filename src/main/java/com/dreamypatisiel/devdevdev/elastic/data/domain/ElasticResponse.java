package com.dreamypatisiel.devdevdev.elastic.data.domain;

public record ElasticResponse<T>(T content, Float score) {
}
