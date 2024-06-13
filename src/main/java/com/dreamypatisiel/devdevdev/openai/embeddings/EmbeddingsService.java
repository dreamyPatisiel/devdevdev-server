package com.dreamypatisiel.devdevdev.openai.embeddings;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.openai.response.Embedding;
import com.dreamypatisiel.devdevdev.openai.response.OpenAIResponse;
import com.dreamypatisiel.devdevdev.openai.response.PickWithSimilarityDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmbeddingsService {

    private final PickRepository pickRepository;

    @Transactional
    public Long saveEmbedding(Long pickId, OpenAIResponse<Embedding> embeddingOpenAIResponse) {

        List<Double> embeddings = embeddingOpenAIResponse.getData().stream()
                .flatMap(data -> data.getEmbedding().stream())
                .toList();

        // 픽픽픽 조회
        Pick findPick = pickRepository.findById(pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_MESSAGE));

        findPick.changeEmbeddings(embeddings);

        return findPick.getId();
    }

    /**
     * @Note: 인자로 사용한 픽픽픽을 제외한 픽픽픽 유사도를 계산한 값과 픽픽픽을 반환한다.
     * @Author: 장세웅
     * @Since: 2024.06.11
     */
    public List<PickWithSimilarityDto> getPicksWithSimilarityDtoExcludeTargetPick(Pick targetPick) {

        // 승인된 픽픽픽 중에 최신순으로 1000개 까지만 조회한다.
        List<Pick> picks = pickRepository.findTop1000ByContentStatusOrderByCreatedAtDesc(ContentStatus.APPROVAL);

        return picks.stream()
                .map(findPick -> new PickWithSimilarityDto(findPick,
                        CosineSimilarityCalculator.cosineSimilarity(targetPick.getEmbeddings(),
                                findPick.getEmbeddings()))
                )
                .filter(similarPick -> !similarPick.getPick().isEqualsId(targetPick.getId()))
                .toList();
    }
}
