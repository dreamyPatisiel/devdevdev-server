package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import com.dreamypatisiel.devdevdev.elastic.data.domain.ElasticResponse;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticTechArticleService;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
public class MemberTechArticleService extends TechArticleCommonService implements TechArticleService {

    private final ElasticTechArticleService elasticTechArticleService;
    private final MemberRepository memberRepository;

    public MemberTechArticleService(TechArticleRepository techArticleRepository, ElasticTechArticleService elasticTechArticleService, MemberRepository memberRepository) {
        super(techArticleRepository);
        this.elasticTechArticleService = elasticTechArticleService;
        this.memberRepository = memberRepository;
    }

    @Override
    public Slice<TechArticleResponse> findTechArticles(Pageable pageable, String elasticId,
                                                       TechArticleSort techArticleSort, Authentication authentication) {
        // 기술블로그 조회
        SearchHits<ElasticTechArticle> searchHits = elasticTechArticleService.findTechArticles(pageable, elasticId, techArticleSort);

        // 회원 조회
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String email = userPrincipal.getEmail();
        SocialType socialType = userPrincipal.getSocialType();
        Member member = memberRepository.findMemberByEmailAndSocialType(new Email(email), socialType)
                .orElseThrow(() -> new MemberException(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE));

        // 데이터 가공
        List<TechArticleResponse> techArticleResponses = getTechArticleResponses(searchHits, member);

        return createElasticSlice(pageable, searchHits, techArticleResponses);
    }

    private List<TechArticleResponse> getTechArticleResponses(SearchHits<ElasticTechArticle> searchHits, Member member) {
        List<ElasticResponse<ElasticTechArticle>> elasticResponses = mapToElasticResponses(searchHits);
        return mapToTechArticleResponses(elasticResponses, member);
    }

    private List<TechArticleResponse> mapToTechArticleResponses(List<ElasticResponse<ElasticTechArticle>> elasticResponses, Member member) {
        List<String> elasticIds = getElasticIds(elasticResponses);
        List<TechArticle> findTechArticles = getTechArticlesByElasticIdsIn(elasticIds);
        Map<String, ElasticResponse<ElasticTechArticle>> elasticsResponseMap = getElasticResponseMap(elasticResponses);

        return findTechArticles.stream()
                .map(findTechArticle -> {
                    ElasticResponse<ElasticTechArticle> elasticResponse = elasticsResponseMap.get(findTechArticle.getElasticId());
                    return TechArticleResponse.of(elasticResponse.content(), findTechArticle, isBookmarkedByMember(findTechArticle, member));
                })
                .toList();
    }

    private boolean isBookmarkedByMember(TechArticle techArticle, Member member) {
        return techArticle.getBookmarks().stream()
                .anyMatch(bookmark -> bookmark.getMember().isEqualsMember(member));
    }
}