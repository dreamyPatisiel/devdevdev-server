package com.dreamypatisiel.devdevdev.domain.repository.member.memberNicknameDictionary;

import com.dreamypatisiel.devdevdev.domain.entity.MemberNicknameDictionary;
import com.dreamypatisiel.devdevdev.domain.repository.member.memberNicknameDictionary.custom.MemberNicknameDictionaryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberNicknameDictionaryRepository extends JpaRepository<MemberNicknameDictionary, Long>,
        MemberNicknameDictionaryRepositoryCustom {
}
