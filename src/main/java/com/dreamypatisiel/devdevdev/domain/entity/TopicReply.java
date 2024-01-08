package com.dreamypatisiel.devdevdev.domain.entity;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContent;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TopicReply extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "commentContent",
            column = @Column(name = "content")
    )
    private CommentContent content;
    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "blame_total_count")
    )
    private Count blameTotalCount;
    @Embedded
    @AttributeOverride(name = "count",
            column = @Column(name = "recommend_total_count")
    )
    private Count recommendTotalCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;
}
