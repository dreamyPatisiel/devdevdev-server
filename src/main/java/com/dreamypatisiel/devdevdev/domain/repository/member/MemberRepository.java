package com.dreamypatisiel.devdevdev.domain.repository.member;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findMemberByUserIdAndSocialType(String userId, SocialType socialType);

    List<Member> findMembersByUserIdAndSocialType(String userId, SocialType socialType);

    Optional<Member> findMemberByEmailAndSocialTypeAndIsDeletedIsFalse(Email email, SocialType socialType);

    Optional<Member> findMemberByRefreshToken(String refreshToken);
}
