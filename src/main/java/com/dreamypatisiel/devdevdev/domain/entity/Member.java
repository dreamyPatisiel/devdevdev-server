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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE member SET is_deleted = true, deletedAt = NOW() WHERE id = ?")
//@SQLRestriction("is_deleted = false")
@Table(indexes = {
        @Index(name = "idx__name__user_id", columnList = "name, userId"),
        @Index(name = "idx__email__socialType", columnList = "email, socialType")
})
public class Member extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String name;

    @Embedded
    @AttributeOverride(name = "nickname",
            column = @Column(name = "nickname", length = 30)
    )
    private Nickname nickname;

    @Embedded
    @AttributeOverride(name = "email",
            column = @Column(name = "email", length = 50)
    )
    private Email email;

    private String password;

    private String userId;

    private String profileImage;

    @Column(length = 50)
    private String job;

    @Embedded
    private AnnualIncome annualIncome;

    @Embedded
    private Experience experience;

    private Boolean subscriptionLetterGranted;

    private String refreshToken;

    @Embedded
    @AttributeOverride(name = "email",
            column = @Column(name = "subscription_letter_email", length = 50)
    )
    private Email subscriptionLetterEmail;

    private LocalDateTime loginDate;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
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
    private List<TechArticleRecommend> recommends = new ArrayList<>();
    
    public Member(Long id) {
        this.id = id;
    }

    @Builder
    private Member(String name, Nickname nickname, Email email, String password, String userId, String profileImage,
                   String job, AnnualIncome annualIncome, Experience experience, Boolean subscriptionLetterGranted,
                   String refreshToken, Email subscriptionLetterEmail, LocalDateTime loginDate, SocialType socialType,
                   Role role, Boolean isDeleted, LocalDateTime deletedAt, List<InterestedCompany> interestedCompanies,
                   List<Notification> notifications, List<Subscription> subscriptions, List<Bookmark> bookmarks,
                   List<TechArticleRecommend> recommends) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.userId = userId;
        this.profileImage = profileImage;
        this.job = job;
        this.annualIncome = annualIncome;
        this.experience = experience;
        this.subscriptionLetterGranted = subscriptionLetterGranted;
        this.refreshToken = refreshToken;
        this.subscriptionLetterEmail = subscriptionLetterEmail;
        this.loginDate = loginDate;
        this.socialType = socialType;
        this.role = role;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
        this.interestedCompanies = interestedCompanies;
        this.notifications = notifications;
        this.subscriptions = subscriptions;
        this.bookmarks = bookmarks;
        this.recommends = recommends;
    }

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

    public String getNicknameAsString() {
        return nickname.getNickname();
    }

    public boolean isEqualsId(Long id) {
        return this.id.equals(id);
    }

    public boolean isAdmin() {
        return this.role.equals(Role.ROLE_ADMIN);
    }

    public void deleteMember(LocalDateTime now) {
        this.isDeleted = true;
        this.deletedAt = now;
    }
}
