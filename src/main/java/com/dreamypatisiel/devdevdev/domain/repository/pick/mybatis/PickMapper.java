package com.dreamypatisiel.devdevdev.domain.repository.pick.mybatis;

import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSearchDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PickMapper {
    List<PickSearchDto> findPickSearchDtoByKeywordAndCursor(@Param("cursorId") Long pickId,
                                                            @Param("keyword") String keyword,
                                                            @Param("cursorScore") Double maxTotalScore,
                                                            @Param("limit") int limit);
}
