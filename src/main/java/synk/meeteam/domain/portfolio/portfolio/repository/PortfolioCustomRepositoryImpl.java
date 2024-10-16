package synk.meeteam.domain.portfolio.portfolio.repository;

import static synk.meeteam.domain.common.field.entity.QField.field;
import static synk.meeteam.domain.common.role.entity.QRole.role;
import static synk.meeteam.domain.portfolio.portfolio.entity.QPortfolio.portfolio;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import synk.meeteam.domain.portfolio.portfolio.dto.QSimplePortfolioDto;
import synk.meeteam.domain.portfolio.portfolio.dto.SimplePortfolioDto;
import synk.meeteam.domain.portfolio.portfolio.entity.Portfolio;
import synk.meeteam.domain.user.user.entity.User;
import synk.meeteam.global.entity.DeleteStatus;

@Repository
@RequiredArgsConstructor
public class PortfolioCustomRepositoryImpl implements PortfolioCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Portfolio> findAllByCreatedByAndIsPinTrueOrderByIds(Long userId, List<Long> portfolioIds) {
        if (portfolioIds.isEmpty()) {
            return new ArrayList<>();
        }

        return queryFactory
                .selectFrom(portfolio)
                .where(portfolio.id.in(portfolioIds),
                        portfolio.createdBy.eq(userId),
                        isAlive()
                )
                .orderBy(orderByPin(portfolioIds).asc())
                .fetch();
    }

    @Override
    public Slice<SimplePortfolioDto> findSlicePortfoliosByUserOrderByCreatedAtDesc(Pageable pageable, User user) {
        List<SimplePortfolioDto> contents = getSimplePortfolios(user, pageable, pageable.getPageSize() + 1);
        return new SliceImpl<>(contents, pageable, hasNextPage(contents, pageable.getPageSize()));
    }

    @Override
    public Page<SimplePortfolioDto> findPaginationPortfoliosByUserOrderByCreatedAtDesc(Pageable pageable, User user) {
        List<SimplePortfolioDto> contents = getSimplePortfolios(user, pageable, pageable.getPageSize());
        JPAQuery<Long> countQuery = getCount(user);
        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    private List<SimplePortfolioDto> getSimplePortfolios(User user, Pageable pageable, int limit) {
        return queryFactory
                .select(new QSimplePortfolioDto(
                        portfolio.id,
                        portfolio.title,
                        portfolio.mainImageFileName,
                        portfolio.field.name,
                        portfolio.role.name,
                        portfolio.isPin,
                        portfolio.pinOrder,
                        portfolio.version
                ))
                .from(portfolio)
                .leftJoin(portfolio.role, role)
                .leftJoin(portfolio.field, field)
                .where(portfolio.createdBy.eq(user.getId()),
                        isAlive())
                .orderBy(portfolio.createdAt.desc(), portfolio.id.desc())
                .offset(pageable.getOffset())
                .limit(limit)
                .fetch();
    }


    private boolean hasNextPage(List<SimplePortfolioDto> contents, int pageSize) {
        if (contents.size() > pageSize) {
            contents.remove(pageSize);
            return true;
        }
        return false;
    }

    private JPAQuery<Long> getCount(User user) {
        return queryFactory.select(portfolio.countDistinct())
                .from(portfolio)
                .where(portfolio.createdBy.eq(user.getId()), isAlive());
    }

    NumberExpression<Integer> orderByPin(List<Long> ids) {
        // 포트폴리오 ID의 순서를 기준으로 순서를 정의합니다.
        CaseBuilder caseBuilder = new CaseBuilder();

        return caseBuilder
                .when(portfolio.id.in(ids)).then(ids.indexOf(portfolio.id))
                .otherwise(ids.size());
    }

    BooleanExpression isAlive() {
        return portfolio.deleteStatus.eq(DeleteStatus.ALIVE);
    }

}
