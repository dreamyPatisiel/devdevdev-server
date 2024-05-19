package com.dreamypatisiel.devdevdev.domain.repository.member.memberNicknameDictionary.custom;

import com.dreamypatisiel.devdevdev.domain.entity.MemberNicknameDictionary;
import com.dreamypatisiel.devdevdev.domain.entity.enums.WordType;
import java.util.Optional;

public interface MemberNicknameDictionaryRepositoryCustom {
    Optional<MemberNicknameDictionary> findRandomWordByWordType(WordType wordType);
}
