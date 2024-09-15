package com.dreamypatisiel.devdevdev.domain.service.common;

import com.dreamypatisiel.devdevdev.domain.repository.BlameRepository;
import com.dreamypatisiel.devdevdev.domain.repository.BlameTypeRepository;
import com.dreamypatisiel.devdevdev.web.dto.response.common.BlameTypeResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberBlameService {

    private final BlameRepository blameRepository;
    private final BlameTypeRepository blameTypeRepository;

    /**
     * @Note: 신고 사유를 조회합니다.
     * @Author: 장세웅
     * @Since: 2024.09.11
     */
    public List<BlameTypeResponse> findBlameType() {
        return blameTypeRepository.findAllByOrderBySortOrderAsc().stream()
                .map(BlameTypeResponse::from)
                .toList();
    }
}
