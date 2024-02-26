package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.response.PickOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PicksResponse;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestPickService implements PickService {

    public static final String INVALID_FIND_PICKS_METHODS_CALL_MESSAGE = "익명 사용자가 아닙니다. 잘못된 메소드 호출 입니다.";

    private final PickRepository pickRepository;

    @Override
    public Slice<PicksResponse> findPicksMain(Pageable pageable, Long pickId, PickSort pickSort, Authentication authentication) {
        if(!AuthenticationMemberUtils.isAnonymous(authentication)) {
            throw new IllegalStateException(INVALID_FIND_PICKS_METHODS_CALL_MESSAGE);
        }

        // 픽픽픽 조회
        Slice<Pick> picks = pickRepository.findPicksByLoePickId(pageable, pickId, pickSort);

        // 데이터 가공
        List<PicksResponse> picksResponses = picks.stream()
                .map(this::mapToPickResponse)
                .toList();

        return new SliceImpl<>(picksResponses, pageable, picks.hasNext());
    }

    private PicksResponse mapToPickResponse(Pick pick) {
        return PicksResponse.builder()
                .id(pick.getId())
                .title(pick.getTitle())
                .voteTotalCount(pick.getVoteTotalCount())
                .commentTotalCount(pick.getCommentTotalCount())
                .pickOptions(mapToPickOptionsResponse(pick))
                .build();
    }

    private List<PickOptionResponse> mapToPickOptionsResponse(Pick pick) {
        return pick.getPickOptions().stream()
                .map(pickOption -> mapToPickOptionResponse(pick, pickOption))
                .toList();
    }

    private PickOptionResponse mapToPickOptionResponse(Pick pick, PickOption pickOption) {
        return PickOptionResponse.builder()
                .id(pickOption.getId())
                .title(pickOption.getTitle())
                .percent(PickOption.calculatePercentBy(pick, pickOption))
                .build();
    }
}
