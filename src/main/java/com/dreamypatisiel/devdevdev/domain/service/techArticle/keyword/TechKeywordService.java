package com.dreamypatisiel.devdevdev.domain.service.techArticle.keyword;

import com.dreamypatisiel.devdevdev.domain.entity.TechKeyword;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechKeywordRepository;
import com.dreamypatisiel.devdevdev.global.utils.HangulUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TechKeywordService {
    private final TechKeywordRepository techKeywordRepository;

    /**
     * @Note:
     * @Author: 유소영
     * @Since: 2025.08.13
     * @param prefix
     * @return 검색어(최대 20개)
     */
    public List<String> autocompleteKeyword(String prefix) {
        String processedInput = prefix;

        // 한글이 포함되어 있다면 자/모음 분리
        if (HangulUtils.hasHangul(prefix)) {
            processedInput = HangulUtils.convertToJamo(prefix);
        }

        // 불리언 검색을 위해 토큰 사이에 '+' 연산자 추가
        String booleanPrefix = convertToBooleanSearch(processedInput);
        Pageable pageable = PageRequest.of(0, 20);
        List<TechKeyword> techKeywords = techKeywordRepository.searchKeyword(booleanPrefix, booleanPrefix, pageable);

        // 응답 데이터 가공
        return techKeywords.stream()
                .map(TechKeyword::getKeyword)
                .toList();
    }
    
    /**
     * 불리언 검색을 위해 각 토큰 사이에 '+' 연산자를 추가하는 메서드
     */
    private String convertToBooleanSearch(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return searchTerm;
        }
        
        // 공백을 기준으로 토큰을 분리하고 각 토큰 앞에 '+' 추가
        String[] tokens = searchTerm.trim().split("\\s+");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = "+" + tokens[i];
        }
        return String.join(" ", tokens);
    }
}
