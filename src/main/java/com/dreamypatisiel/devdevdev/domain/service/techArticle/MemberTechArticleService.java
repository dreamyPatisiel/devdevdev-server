package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.response.CompanyResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import com.dreamypatisiel.devdevdev.elastic.data.domain.ElasticResponse;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticTechArticleService;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
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
    private final MemberProvider memberProvider;


    public MemberTechArticleService(TechArticleRepository techArticleRepository,
                                    ElasticTechArticleRepository elasticTechArticleRepository,
                                    ElasticTechArticleService elasticTechArticleService,
                                    MemberProvider memberProvider) {
        super(techArticleRepository, elasticTechArticleRepository);
        this.elasticTechArticleService = elasticTechArticleService;
        this.memberProvider = memberProvider;
    }

    @Override
    public Slice<TechArticleResponse> getTechArticles(Pageable pageable, String elasticId,
                                                         TechArticleSort techArticleSort, String keyword,
                                                         Float score, Authentication authentication) {
        // 기술블로그 조회
        SearchHits<ElasticTechArticle> searchHits = elasticTechArticleService.getTechArticles(pageable, elasticId, techArticleSort, keyword, score);

        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 데이터 가공
        List<TechArticleResponse> techArticleResponses = getTechArticleResponses(searchHits, member);

        return createElasticSlice(pageable, searchHits, techArticleResponses);
    }

    @Override
    public TechArticleResponse getTechArticle(Long id, Authentication authentication) {
        // 기술블로그 조회
        TechArticle techArticle = findTechArticle(id);
        ElasticTechArticle elasticTechArticle = findElasticTechArticle(techArticle.getElasticId());
        CompanyResponse companyResponse = CompanyResponse.from(techArticle.getCompany());

        // 회원 조회
        Member member = memberProvider.getMemberByAuthentication(authentication);

        // 데이터 가공
        return getTechArticleResponse(techArticle, elasticTechArticle, companyResponse, member);
    }

    private List<TechArticleResponse> getTechArticleResponses(SearchHits<ElasticTechArticle> searchHits, Member member) {
        List<ElasticResponse<ElasticTechArticle>> elasticResponses = mapToElasticResponses(searchHits);
        return mapToTechArticleResponses(elasticResponses, member);
    }

    private List<TechArticleResponse> mapToTechArticleResponses(List<ElasticResponse<ElasticTechArticle>> elasticResponses, Member member) {
        List<TechArticle> findTechArticles = getTechArticlesByElasticIdsIn(elasticResponses);
        Map<String, ElasticResponse<ElasticTechArticle>> elasticsResponseMap = getElasticResponseMap(elasticResponses);

        return findTechArticles.stream()
                .map(findTechArticle -> {
                    ElasticResponse<ElasticTechArticle> elasticResponse = elasticsResponseMap.get(findTechArticle.getElasticId());
                    CompanyResponse companyResponse = createCompanyResponse(findTechArticle);
                    return TechArticleResponse.of(elasticResponse.content(), findTechArticle, companyResponse, elasticResponse.score(), isBookmarkedByMember(findTechArticle, member));
                })
                .toList();
    }

    private TechArticleResponse getTechArticleResponse(TechArticle techArticle, ElasticTechArticle elasticTechArticle, CompanyResponse companyResponse, Member member) {
        return TechArticleResponse.of(elasticTechArticle, techArticle, companyResponse, isBookmarkedByMember(techArticle, member));
    }

    private boolean isBookmarkedByMember(TechArticle techArticle, Member member) {
        return techArticle.getBookmarks().stream()
                .anyMatch(bookmark -> bookmark.getMember().isEqualsMember(member));
    }
}