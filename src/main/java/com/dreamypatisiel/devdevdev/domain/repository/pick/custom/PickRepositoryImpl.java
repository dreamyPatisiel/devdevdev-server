package com.dreamypatisiel.devdevdev.domain.repository.pick.custom;

import static com.dreamypatisiel.devdevdev.domain.entity.QMember.member;
import static com.dreamypatisiel.devdevdev.domain.entity.QPick.pick;
import static com.dreamypatisiel.devdevdev.domain.entity.QPickOption.pickOption;
import static com.dreamypatisiel.devdevdev.domain.entity.QPickVote.pickVote;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.ObjectUtils;

@RequiredArgsConstructor
public class PickRepositoryImpl implements PickRepositoryCustom {

    public static final long TWO = 2L;
    private static final int ONE = 1;

    private final JPQLQueryFactory query;

    /**
     * @Note: ToMany 페치 조인과 페이징은 불가 ToOne은 페치 조인, ToMany는 지연로딩으로...! (where in 절) - default_batch_fetch_size: 1000
     */
    @Override
    public Slice<Pick> findPicksByCursor(Pageable pageable, Long pickId, PickSort pickSort) {
        // 1개의 pick에 2개의 pickOtion이 존재하기 때문에 pageSize에 2를 곱해야 한다.
        long limit = pageable.getPageSize() * TWO + ONE;

        List<Pick> contents = query.selectFrom(pick)
                .leftJoin(pick.pickOptions, pickOption)
                .leftJoin(pick.pickVotes, pickVote)
                .leftJoin(pick.member, member).fetchJoin()
                .where(getCursorCondition(pickSort, pickId))
                .orderBy(pickSort(pickSort), pick.id.desc())
                .limit(limit)
                .fetch();

        int pageSize = pageable.getPageSize() * Long.valueOf(TWO).intValue();

        return new SliceImpl<>(contents, pageable, hasNextPage(contents, pageSize));
    }

    @Override
    public Optional<Pick> findPickAndPickOptionByPickId(Long pickId) {
        Pick findPick = query.selectFrom(pick)
                .innerJoin(pick.pickOptions, pickOption).fetchJoin()
                .leftJoin(pick.pickVotes, pickVote)
                .leftJoin(pick.member, member).fetchJoin()
                .where(pick.id.eq(pickId))
                .fetchOne();

        return Optional.ofNullable(findPick);
    }

    @Override
    public Optional<Pick> findPickDetailByPickId(Long pickId) {
        Pick findPick = query.selectFrom(pick)
                .leftJoin(pick.pickOptions, pickOption).fetchJoin()
                .where(pick.id.eq(pickId))
                .fetchOne();

        return Optional.ofNullable(findPick);
    }

    private BooleanExpression getCursorCondition(PickSort pickSort, Long pickId) {
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

        // sort 조건에 맞는 getCursorCondition 반환
        return Arrays.stream(PickSort.values())
                .filter(sort -> sort.equals(pickSort))
                .findFirst()
                .map(sort -> sort.getCursorCondition(findPick))
                .orElse(PickSort.LATEST.getCursorCondition(findPick));
    }

    private OrderSpecifier pickSort(PickSort pickSort) {
        return Optional.ofNullable(pickSort)
                .orElse(PickSort.LATEST).getOrderSpecifierByPickSort();
    }

    private boolean hasNextPage(List<Pick> contents, int pageSize) {
        return contents.size() > pageSize;
    }
}
