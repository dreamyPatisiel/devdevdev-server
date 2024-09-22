package com.dreamypatisiel.devdevdev.domain.repository.blame.custom;

import com.dreamypatisiel.devdevdev.domain.service.blame.dto.BlameDto;

public interface BlameRepositoryCustom {
    Boolean existsBlameByBlameDto(BlameDto blameDto);
}
