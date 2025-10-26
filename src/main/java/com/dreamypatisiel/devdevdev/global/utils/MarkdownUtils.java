package com.dreamypatisiel.devdevdev.global.utils;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;

/**
 * 마크다운 처리를 위한 유틸리티 클래스
 */
public abstract class MarkdownUtils {

    private static final Parser PARSER = Parser.builder().build();
    private static final TextContentRenderer TEXT_RENDERER = TextContentRenderer.builder().build();

    /**
     * 마크다운 텍스트를 순수 텍스트로 변환
     */
    public static String convertMarkdownToText(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return markdown;
        }

        Node document = PARSER.parse(markdown);
        return TEXT_RENDERER.render(document).trim();
    }
}

