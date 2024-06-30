package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Reason;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickBlameType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PickBlame extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @Column(length = 255)
    private Reason reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_comment_id", nullable = false)
    private PickComment pickComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_reply_id", nullable = false)
    private PickReply pickReply;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_id", nullable = false)
    private Pick pick;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(value = EnumType.STRING)
    private PickBlameType pickBlameType;
}
