package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Topic extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String contents;
    private Long viewTotalCount;
    private Long commentTotalCount;
    private String author;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "topic")
    private List<TopicComment> topicComments = new ArrayList<>();
    @OneToMany(mappedBy = "topic")
    private List<TopicReply> topicReplies = new ArrayList<>();

}
