package com.dreamypatisiel.devdevdev.domain.repository.pick;

import static com.dreamypatisiel.devdevdev.domain.entity.QMember.member;
import static com.dreamypatisiel.devdevdev.domain.entity.QPick.pick;
import static com.dreamypatisiel.devdevdev.domain.entity.QPickOption.pickOption;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.ObjectUtils;

/**
 * @Note: ToMany 페치 조인과 페이징은 불가
 * ToOne은 페치 조인, ToMany는 지연로딩으로...!(where in 절)
 */
@RequiredArgsConstructor
public class PickRepositoryImpl implements PickRepositoryCustom {

    private static final int ONE = 1;

    private final JPQLQueryFactory jpqlQueryFactory;

    @Override
    public Slice<Pick> findPicksByLtPickId(Pageable pageable, Long pickId) {
        List<Pick> contents = jpqlQueryFactory.selectFrom(pick)
                .leftJoin(pick.pickOptions, pickOption)
                .leftJoin(pick.member, member).fetchJoin()
                .where(loePickId(pickId))
                .orderBy(pick.id.desc())
                .limit(pageable.getPageSize() + ONE)
                .fetch();

        return new SliceImpl<>(contents, pageable, hasNextPage(contents, pageable.getPageSize()));
    }

    private BooleanExpression loePickId(Long pickId) {
        if (ObjectUtils.isEmpty(pickId)) {
            return null;
        }

        return pick.id.loe(pickId);
    }

    private boolean hasNextPage(List<Pick> contents, int pageSize) {
        if(contents.size() > pageSize) {
            contents.remove(pageSize);
            return true;
        }
        return false;
    }
}
