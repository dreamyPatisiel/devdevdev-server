package com.dreamypatisiel.devdevdev.elastic.data.response;

public record ElasticResponse<T>(T content, Float score) {
}
