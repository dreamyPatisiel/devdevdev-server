package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.*;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx__name__user_id", columnList = "name, userId"),
        @Index(name = "idx__email__socialType", columnList = "email, socialType")
})
public class Member extends BasicTime {


    @Id @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Getter
    private String name;

    @Embedded
    @AttributeOverride(name = "nickname",
            column = @Column(name = "nickname")
    )
    private Nickname nickname;

    @Getter
    @Embedded
    @AttributeOverride(name = "email",
            column = @Column(name = "email")
    )
    private Email email;
    @Getter
    private String password;
    @Getter
    private String userId;
    @Getter
    private String profileImage;
    private String job;
    @Embedded
    private AnnualIncome annualIncome;
    @Embedded
    private Experience experience;
    private Boolean subscriptionLetterGranted;
    private String refreshToken;
    @Embedded
    @AttributeOverride(name = "email",
            column = @Column(name = "subscription_letter_email")
    )
    private Email subscriptionLetterEmail;
    private LocalDateTime loginDate;
    @Getter
    @Enumerated(EnumType.STRING)
    private SocialType socialType;
    @Getter
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany(mappedBy = "member")
    private List<InterestedCompany> interestedCompanies = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Bookmark> bookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Recommend> recommends = new ArrayList<>();

    public static Member createMemberBy(SocialMemberDto socialMemberDto) {
        Member member = new Member();
        member.userId = socialMemberDto.getUserId();
        member.name = socialMemberDto.getName();
        member.email = new Email(socialMemberDto.getEmail());
        member.nickname = new Nickname(socialMemberDto.getNickName());
        member.password = socialMemberDto.getPassword();
        member.socialType = socialMemberDto.getSocialType();
        member.role = socialMemberDto.getRole();

        return member;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean isRefreshTokenEquals(String refreshToken) {
        return refreshToken.equalsIgnoreCase(this.refreshToken);
    }

    public String getEmailAsString() {
        return email.getEmail();
    }
}
