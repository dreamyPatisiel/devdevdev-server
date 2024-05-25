package com.dreamypatisiel.devdevdev.domain.service.member;

import com.dreamypatisiel.devdevdev.domain.entity.MemberNicknameDictionary;
import com.dreamypatisiel.devdevdev.domain.entity.enums.WordType;
import com.dreamypatisiel.devdevdev.domain.repository.member.memberNicknameDictionary.MemberNicknameDictionaryRepository;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberNicknameDictionaryService {

    public static final String NOT_FOUND_WORD_EXCEPTION_MESSAGE = "랜덤 닉네임 생성을 위한 단어가 없습니다.";

    private final MemberNicknameDictionaryRepository memberNicknameDictionaryRepository;

    /**
     * 랜덤 닉네임을 생성합니다.
     */
    public String createRandomNickname() {
        // WordType 우선순위에 따라 랜덤 단어를 1개씩 뽑습니다.
        List<MemberNicknameDictionary> nicknameDictionaryWords = Stream.of(WordType.values())
                .sorted(Comparator.comparingInt(WordType::getPriority)) // 우선순위에 따라 정렬
                .map(this::findRandomWordByWordType)
                .collect(Collectors.toList());

        return concatNickname(nicknameDictionaryWords);
    }

    private MemberNicknameDictionary findRandomWordByWordType(WordType wordType) {
        return memberNicknameDictionaryRepository.findRandomWordByWordType(wordType)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_WORD_EXCEPTION_MESSAGE));
    }

    private String concatNickname(List<MemberNicknameDictionary> words) {
        return words.stream()
                .map(word -> word.getWord().getWord())
                .collect(Collectors.joining(" "));
    }
}