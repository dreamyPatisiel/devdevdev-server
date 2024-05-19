package com.dreamypatisiel.devdevdev.domain.repository.member.memberNicknameDictionary.custom;

import static com.dreamypatisiel.devdevdev.domain.entity.QMemberNicknameDictionary.memberNicknameDictionary;

import com.dreamypatisiel.devdevdev.domain.entity.MemberNicknameDictionary;
import com.dreamypatisiel.devdevdev.domain.entity.enums.WordType;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberNicknameDictionaryRepositoryImpl implements MemberNicknameDictionaryRepositoryCustom {

    private final JPQLQueryFactory query;

    @Override
    public Optional<MemberNicknameDictionary> findRandomWordByWordType(WordType wordType) {

        MemberNicknameDictionary findWord = query.selectFrom(memberNicknameDictionary)
                .where(memberNicknameDictionary.wordType.eq(wordType))
                .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
                .fetchFirst();
        return Optional.ofNullable(findWord);
    }
}
