package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Reason;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechBlame extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Reason reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_comment_id")
    private TechComment techComment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_reply_id")
    private TechReply techReply;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_article_id")
    private TechArticle techArticle;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    @Enumerated(value = EnumType.STRING)
    private TechBlameType techBlameType;
}
