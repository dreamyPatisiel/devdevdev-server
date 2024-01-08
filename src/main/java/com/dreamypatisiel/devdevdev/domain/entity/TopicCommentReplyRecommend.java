package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TopicCommentReplyRecommend extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_comment_id")
    private TopicComment topicComment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_reply_id")
    private TopicReply topicReply;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}