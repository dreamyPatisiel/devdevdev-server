package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticleRecommend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TechArticleRecommendRepository extends JpaRepository<TechArticleRecommend, Long> {
    Optional<TechArticleRecommend> findByTechArticleAndMember(TechArticle techArticle, Member member);

    Optional<TechArticleRecommend> findByTechArticleAndAnonymousMember(TechArticle techArticle, AnonymousMember anonymousMember);
}
