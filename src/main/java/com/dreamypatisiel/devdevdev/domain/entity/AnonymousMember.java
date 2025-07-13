package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx__anonymous_member_id", columnList = "anonymousMemberId")
})
public class AnonymousMember extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false, unique = true)
    private String anonymousMemberId;

    private String nickname;

    @Builder
    private AnonymousMember(String anonymousMemberId) {
        this.anonymousMemberId = anonymousMemberId;
    }

    public static AnonymousMember create(String anonymousMemberId, String nickname) {
        AnonymousMember anonymousMember = new AnonymousMember();
        anonymousMember.anonymousMemberId = anonymousMemberId;
        anonymousMember.nickname = nickname;

        return anonymousMember;
    }

    public boolean isEqualAnonymousMemberId(Long id) {
        return this.id.equals(id);
    }

    public boolean hasNickName() {
        return nickname != null && !nickname.isBlank();
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }
}
