package com.dreamypatisiel.devdevdev.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
                @Index(name = "idx_tech_keyword_01", columnList = "chosung_key"),
                @Index(name = "idx_tech_keyword_02", columnList = "jamo_key")
})
public class TechKeyword extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, columnDefinition = "varchar(100) COLLATE utf8mb4_bin")
    private String keyword;

    @Column(nullable = false, length = 300, columnDefinition = "varchar(300) COLLATE utf8mb4_bin")
    private String jamoKey;

    @Column(nullable = false, length = 150, columnDefinition = "varchar(150) COLLATE utf8mb4_bin")
    private String chosungKey;

    @Builder
    private TechKeyword(String keyword, String jamoKey, String chosungKey) {
        this.keyword = keyword;
        this.jamoKey = jamoKey;
        this.chosungKey = chosungKey;
    }
}