package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = @Index(name = "idx__name__email",
        columnList = "name, email"))
public class Member extends BasicTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Embedded
    @AttributeOverride(name = "email",
            column = @Column(name = "email")
    )
    private Email email;
    @Embedded
    private Password password;
    private String userId;
    private String profileImage;
    private String job;
    @Embedded
    private AnnualIncome annualIncome;
    @Embedded
    private Experience experience;
    private Boolean subscriptionLetterGranted;
    @Embedded
    @AttributeOverride(name = "email",
            column = @Column(name = "subscription_letter_email")
    )
    private Email subscriptionLetterEmail; //
    private LocalDateTime loginDate;

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


}
