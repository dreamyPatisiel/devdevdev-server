package com.dreamypatisiel.devdevdev.domain.service.member;

import static org.assertj.core.api.Assertions.assertThat;

import com.dreamypatisiel.devdevdev.domain.entity.MemberNicknameDictionary;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Word;
import com.dreamypatisiel.devdevdev.domain.entity.enums.WordType;
import com.dreamypatisiel.devdevdev.domain.repository.member.memberNicknameDictionary.MemberNicknameDictionaryRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberNicknameDictionaryServiceTest {

    @Autowired
    MemberNicknameDictionaryService memberNicknameDictionaryService;
    @Autowired
    MemberNicknameDictionaryRepository memberNicknameDictionaryRepository;

    @Test
    @DisplayName("WordType 우선순위에 따라 랜덤 닉네임이 생성된다.")
    void test() {
        // given
        List<MemberNicknameDictionary> nicknameDictionaryWords = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            for (WordType wordType : WordType.values()) {
                nicknameDictionaryWords.add(createMemberNicknameDictionary(wordType.getType() + i, wordType));
            }
        }
        memberNicknameDictionaryRepository.saveAll(nicknameDictionaryWords);

        // when
        String randomNickname = memberNicknameDictionaryService.createRandomNickname();

        // then
        assertThat(randomNickname.split(" "))
                .hasSize(WordType.values().length);
    }

    private static MemberNicknameDictionary createMemberNicknameDictionary(String word, WordType wordType) {
        return MemberNicknameDictionary.builder()
                .word(new Word(word))
                .wordType(wordType)
                .build();
    }
}