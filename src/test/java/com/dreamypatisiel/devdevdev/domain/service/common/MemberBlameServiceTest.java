package com.dreamypatisiel.devdevdev.domain.service.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.dreamypatisiel.devdevdev.domain.entity.BlameType;
import com.dreamypatisiel.devdevdev.domain.repository.BlameRepository;
import com.dreamypatisiel.devdevdev.domain.repository.BlameTypeRepository;
import com.dreamypatisiel.devdevdev.web.dto.response.common.BlameTypeResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberBlameServiceTest {

    @Autowired
    MemberBlameService memberBlameService;
    @Autowired
    BlameTypeRepository blameTypeRepository;
    @Autowired
    BlameRepository blameRepository;

    @Test
    @DisplayName("모든 신고 사유를 조회합니다.")
    void findCreateBlameType() {
        // given
        BlameType blameType1 = createBlameType("욕설1", 0);
        BlameType blameType2 = createBlameType("욕설2", 1);
        BlameType blameType3 = createBlameType("욕설3", 2);
        BlameType blameType4 = createBlameType("욕설4", 3);

        blameTypeRepository.saveAll(List.of(blameType1, blameType2, blameType3, blameType4));

        // when
        List<BlameTypeResponse> blameTypes = memberBlameService.findBlameType();

        // then
        assertThat(blameTypes).hasSize(4)
                .extracting("id", "reason", "sortOrder")
                .containsExactly(
                        tuple(blameType1.getId(), "욕설1", 0),
                        tuple(blameType2.getId(), "욕설2", 1),
                        tuple(blameType3.getId(), "욕설3", 2),
                        tuple(blameType4.getId(), "욕설4", 3)
                );
    }

    private BlameType createBlameType(String reason, int sortOrder) {
        return new BlameType(reason, sortOrder);
    }
}