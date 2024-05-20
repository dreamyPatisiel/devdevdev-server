package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.AnnualIncome;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Experience;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Nickname;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE member SET is_deleted = true, deletedAt = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Table(indexes = {
        @Index(name = "idx__name__user_id", columnList = "name, userId"),
        @Index(name = "idx__email__socialType", columnList = "email, socialType")
})
public class Member extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Embedded
    @AttributeOverride(name = "nickname",
            column = @Column(name = "nickname")
    )
    private Nickname nickname;


    @Embedded
    @AttributeOverride(name = "email",
            column = @Column(name = "email")
    )
    private Email email;

    private String password;

    private String userId;

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

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Boolean isDeleted;
    private LocalDateTime deletedAt;

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
        member.nickname = new Nickname(socialMemberDto.getNickname());
        member.password = socialMemberDto.getPassword();
        member.socialType = socialMemberDto.getSocialType();
        member.role = socialMemberDto.getRole();
        member.isDeleted = false;
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

    public boolean isEqualMember(Member member) {
        return this.equals(member);
    }

    public boolean isAdmin() {
        return this.role.equals(Role.ROLE_ADMIN);
    }

    public void deleteMember(LocalDateTime now) {
        this.isDeleted = true;
        this.deletedAt = now;
    }
}
