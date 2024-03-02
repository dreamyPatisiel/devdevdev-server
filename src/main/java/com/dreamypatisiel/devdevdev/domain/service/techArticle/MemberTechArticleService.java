package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.repository.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberTechArticleService implements TechArticleService {
    BookmarkRepository bookmarkRepository;
    MemberRepository memberRepository;
    @Override
    public Slice<TechArticleResponse> getTechArticles(Pageable pageable, String keyword, Authentication authentication) {

        // 게시글 조회
        List<TechArticleResponse> responses = new ArrayList<>();

        // 회원 조회
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String email = userPrincipal.getEmail();
        SocialType socialType = userPrincipal.getSocialType();
        Member member = memberRepository.findMemberByEmailAndSocialType(new Email(email), socialType)
                .orElseThrow(() -> new MemberException(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE));

        // 북마크 여부 조회




        // 데이터 가공
        // TechArticle 조회
        List<TechArticleResponse> techArticleResponses = new ArrayList<>();


        return null;
    }

}