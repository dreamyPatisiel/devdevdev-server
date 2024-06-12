package com.dreamypatisiel.devdevdev.openai.embeddings;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.openai.request.EmbeddingRequest;
import com.dreamypatisiel.devdevdev.openai.response.Embedding;
import com.dreamypatisiel.devdevdev.openai.response.OpenAIResponse;
import com.dreamypatisiel.devdevdev.openai.response.PickWithSimilarityDto;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class EmbeddingsServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    PickRepository pickRepository;
    @Autowired
    EmbeddingRequestHandler embeddingRequestHandler;
    @Autowired
    EmbeddingsService embeddingsService;

    @Disabled
    @Test
    @DisplayName("인자로 사용한 픽픽픽을 제외한 픽픽픽 유사도를 계산한 값과 픽픽픽을 반환한다.")
    void getPicksWithSimilarityDtoExcludeTargetPick() {

        // given
        Pick pick = setupPickAndEmbedding("스벨트 VS 리액트");
        setupPickAndEmbedding("Svelte 그리고 React");
        setupPickAndEmbedding("what is the best frontend framework?");
        setupPickAndEmbedding("best frontend framework?");
        setupPickAndEmbedding("what is the best backend framework?");
        setupPickAndEmbedding("최고의 프론트엔드 프레임워크는 무엇일까?");
        setupPickAndEmbedding("가장 많이 사용하는 백엔드 프레임워크는 무엇일까?");
        setupPickAndEmbedding("facebook");
        setupPickAndEmbedding("스벨트와 리액트");
        setupPickAndEmbedding("리액트와 스벨트 누가 최고인가?");
        setupPickAndEmbedding("React Vs Svelte Vs Vue");
        setupPickAndEmbedding("Vue3 이대로 괜찮은가?");
        setupPickAndEmbedding("리액트가 정말로 최고 같아?");
        setupPickAndEmbedding("JPA는 필수인가?");
        setupPickAndEmbedding("스프링이 백엔드의 왕인가?");
        setupPickAndEmbedding("유소영은 천사인가?");

        em.flush();
        em.clear();

        // when
        List<PickWithSimilarityDto> PicksWithSimilarityDto = embeddingsService.getPicksWithSimilarityDtoExcludeTargetPick(
                pick);

        // then
        PicksWithSimilarityDto.forEach(pickWithSimilarityDto -> {
            System.out.println("제목=" + pickWithSimilarityDto.getPick().getTitle().getTitle());
            System.out.println("유사도=" + pickWithSimilarityDto.getSimilarity());
        });
    }

    private Pick setupPickAndEmbedding(String title) {
        Pick pick = createPick(title);
        pickRepository.save(pick);

        long start = System.currentTimeMillis();
        EmbeddingRequest request = EmbeddingRequest.createTextEmbedding3Small(pick.getTitle().getTitle());
        OpenAIResponse<Embedding> embeddingOpenAIResponse = embeddingRequestHandler.postEmbeddings(request);
        long end = System.currentTimeMillis();
        System.out.println("요청 시간=" + (end - start));

        long start2 = System.currentTimeMillis();
        embeddingsService.saveEmbedding(pick, embeddingOpenAIResponse);
        long end2 = System.currentTimeMillis();
        System.out.println("저장 시간=" + (end2 - start2));

        return pick;
    }

    private static Pick createPick(String title) {
        return Pick.builder()
                .title(new Title(title))
                .build();
    }
}