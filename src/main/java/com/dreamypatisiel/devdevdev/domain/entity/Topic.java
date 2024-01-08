package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.TopicContents;
import jakarta.persistence.*;
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

    @Column(length = 50)
    private Title title;

    @Embedded
    @AttributeOverride(name = "topicContents",
            column = @Column(name = "contents")
    )
    private TopicContents contents;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "view_total_count")
    )
    private Count viewTotalCount;

    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "comment_total_count")
    )
    private Count commentTotalCount;
    private String thumbnailUrl;
    private String author;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "topic")
    private List<TopicComment> topicComments = new ArrayList<>();
    @OneToMany(mappedBy = "topic")
    private List<TopicReply> topicReplies = new ArrayList<>();

}
