package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.SimilarPickResponse;
import com.dreamypatisiel.devdevdev.exception.InternalServerException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.openai.embeddings.EmbeddingsService;
import com.dreamypatisiel.devdevdev.openai.response.PickWithSimilarityDto;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PickCommonService {

    private static final int SIMILARITY_PICK_MAX_COUNT = 3;

    private final PickRepository pickRepository;
    private final EmbeddingsService embeddingsService;

    public List<SimilarPickResponse> findTop3SimilarPicks(Long pickId) {

        // 픽픽픽 조회
        Pick findPick = pickRepository.findById(pickId)
                .orElseThrow(() -> new NotFoundException(INVALID_NOT_FOUND_PICK_MESSAGE));

        // 픽픽픽 게시글의 승인 상태가 아니면
        if (!findPick.isTrueContentStatus(ContentStatus.APPROVAL)) {
            throw new IllegalArgumentException(INVALID_NOT_APPROVAL_STATUS_PICK_MESSAGE);
        }

        // 임베딩 값이 없으면 500 예외 발생
        if (ObjectUtils.isEmpty(findPick.getEmbeddings())) {
            throw new InternalServerException();
        }

        // 유사도를 계산한 픽픽픽 조회
        List<PickWithSimilarityDto> pickWithSimilarityDto = embeddingsService.getPicksWithSimilarityDtoExcludeTargetPick(
                findPick);

        return pickWithSimilarityDto.stream()
                .map(SimilarPickResponse::from)
                .sorted(Comparator.comparingDouble(SimilarPickResponse::getSimilarity).reversed()) // 내림차순
                .limit(SIMILARITY_PICK_MAX_COUNT)
                .toList();
    }
}
