package com.dreamypatisiel.devdevdev.web.docs.format;

import static org.springframework.restdocs.snippet.Attributes.key;

import org.springframework.restdocs.snippet.Attributes;

public interface ApiDocsFormatGenerator {
    static Attributes.Attribute authenticationType() {
        return key("authentication").value("O");
    }
}
