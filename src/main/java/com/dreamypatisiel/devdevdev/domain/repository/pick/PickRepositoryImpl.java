package com.dreamypatisiel.devdevdev.domain.repository.pick;

import static com.dreamypatisiel.devdevdev.domain.entity.QMember.member;
import static com.dreamypatisiel.devdevdev.domain.entity.QPick.pick;
import static com.dreamypatisiel.devdevdev.domain.entity.QPickOption.pickOption;
import static com.dreamypatisiel.devdevdev.domain.entity.QPickVote.pickVote;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.ObjectUtils;

/**
 * @Note: ToMany 페치 조인과 페이징은 불가
 * ToOne은 페치 조인, ToMany는 지연로딩으로...! (where in 절)
 *  - default_batch_fetch_size: 1000
 */
@RequiredArgsConstructor
public class PickRepositoryImpl implements PickRepositoryCustom {

    public static final long TWO = 2L;
    private static final int ONE = 1;

    private final JPQLQueryFactory query;

    @Override
    public Slice<Pick> findPicksByCursor(Pageable pageable, Long pickId, PickSort pickSort) {
        // 1개의 pick에 2개의 pickOtion이 존재하기 때문에 pageSize에 2를 곱해야 한다.
        long limit = pageable.getPageSize() * TWO + ONE;

        List<Pick> contents = query.selectFrom(pick)
                .leftJoin(pick.pickOptions, pickOption)
                .leftJoin(pick.pickVotes, pickVote)
                .leftJoin(pick.member, member).fetchJoin()
                .where(cursorCondition(pickSort, pickId))
                .orderBy(pickSort(pickSort), pick.id.desc())
                .limit(limit)
                .fetch();

        int pageSize = pageable.getPageSize() * Long.valueOf(TWO).intValue();

        return new SliceImpl<>(contents, pageable, hasNextPage(contents, pageSize));
    }

    private BooleanExpression cursorCondition(PickSort pickSort, Long pickId) {
        if (ObjectUtils.isEmpty(pickId)) {
            return null;
        }

        // 픽 아이디로 픽 조회
        Pick findPick = query.selectFrom(pick)
                .where(pick.id.eq(pickId))
                .fetchOne();

        // 픽이 없으면
        if (ObjectUtils.isEmpty(findPick)) {
            return pick.id.loe(pickId);
        }

        // sort 조건에 맞는 cursorCondition 반환
        return Arrays.stream(PickSort.values())
                .filter(sort -> sort.equals(pickSort))
                .findFirst()
                .map(sort -> sort.getCursorCondition(findPick))
                .orElse(PickSort.LATEST.getCursorCondition(findPick));
    }

    @Deprecated
    private BooleanExpression loePickId(Long pickId) {
        if (ObjectUtils.isEmpty(pickId)) {
            return null;
        }

        return pick.id.loe(pickId);
    }

    private OrderSpecifier pickSort(PickSort pickSort) {
        return Arrays.stream(PickSort.values())
                .filter(sort -> sort.equals(pickSort))
                .findFirst()
                .map(PickSort::getOrderSpecifierByPickSort)
                .orElseGet(PickSort.LATEST::getOrderSpecifierByPickSort);
    }

    private boolean hasNextPage(List<Pick> contents, int pageSize) {
        return contents.size() > pageSize;
    }
}
