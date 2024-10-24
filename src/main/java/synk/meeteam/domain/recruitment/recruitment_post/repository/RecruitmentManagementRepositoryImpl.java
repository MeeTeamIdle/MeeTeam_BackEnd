package synk.meeteam.domain.recruitment.recruitment_post.repository;

import static synk.meeteam.domain.recruitment.bookmark.entity.QBookmark.bookmark;
import static synk.meeteam.domain.recruitment.recruitment_applicant.entity.QRecruitmentApplicant.recruitmentApplicant;
import static synk.meeteam.domain.recruitment.recruitment_post.entity.QRecruitmentPost.recruitmentPost;
import static synk.meeteam.domain.recruitment.recruitment_post.repository.RecruitmentManagementRepositoryImpl.managementJpaUtils.joinByManagementType;
import static synk.meeteam.domain.recruitment.recruitment_post.repository.expression.RecruitmentExpressionUtils.isClosedEq;
import static synk.meeteam.domain.recruitment.recruitment_post.repository.expression.RecruitmentExpressionUtils.isNotDeleted;
import static synk.meeteam.domain.recruitment.recruitment_post.repository.expression.RecruitmentExpressionUtils.isWriter;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import synk.meeteam.domain.recruitment.recruitment_post.dto.ManageType;
import synk.meeteam.domain.recruitment.recruitment_post.repository.expression.RecruitmentExpressionUtils;
import synk.meeteam.domain.recruitment.recruitment_post.repository.vo.QRecruitmentPostVo;
import synk.meeteam.domain.recruitment.recruitment_post.repository.vo.RecruitmentPostVo;
import synk.meeteam.domain.user.user.entity.QUser;
import synk.meeteam.domain.user.user.entity.User;
import synk.meeteam.global.entity.DeleteStatus;

@Repository
@RequiredArgsConstructor
public class RecruitmentManagementRepositoryImpl implements RecruitmentManagementRepository {
    private final JPAQueryFactory queryFactory;

    public Page<RecruitmentPostVo> findManagementPost(Pageable pageable, User user, Boolean isClosed,
                                                      ManageType manageType) {
        List<RecruitmentPostVo> contents = getManagementPostVos(pageable, user, isClosed, manageType);
        JPAQuery<Long> countQuery = getManagementPostCount(user, isClosed, manageType);
        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    private List<RecruitmentPostVo> getManagementPostVos(Pageable pageable, User userDomain, Boolean isClosed,
                                                         ManageType manageType) {
        QUser writer = new QUser("writer");

        JPAQuery<RecruitmentPostVo> query = queryFactory.select(
                        new QRecruitmentPostVo(
                                recruitmentPost.id,
                                recruitmentPost.title,
                                recruitmentPost.category,
                                recruitmentPost.scope,
                                writer.id,
                                writer.nickname,
                                writer.profileImgFileName,
                                recruitmentPost.deadline,
                                manageType == ManageType.BOOKMARKED ? Expressions.asBoolean(true)
                                        : RecruitmentExpressionUtils.isBookmark(userDomain),
                                recruitmentPost.createdAt,
                                recruitmentPost.isClosed,
                                writer.imgVersion
                        )
                )
                .distinct()
                .from(recruitmentPost)
                .leftJoin(writer).on(recruitmentPost.createdBy.eq(writer.id))
                .where(
                        isClosedEq(isClosed),
                        isNotDeleted(),
                        isWriter(manageType, userDomain, writer)
                );

        joinByManagementType(userDomain, manageType, query);

        return query.orderBy(recruitmentPost.createdAt.desc(), recruitmentPost.id.desc())
                .offset(pageable.getOffset()) //페이지 번호
                .limit(pageable.getPageSize()) //페이지 사이즈
                .fetch();
    }

    private JPAQuery<Long> getManagementPostCount(User userDomain, Boolean isClosed, ManageType manageType) {
        QUser writer = new QUser("writer");

        JPAQuery<Long> query = queryFactory.select(recruitmentPost.countDistinct())
                .from(recruitmentPost)
                .leftJoin(writer).on(recruitmentPost.createdBy.eq(writer.id))
                .leftJoin(bookmark).on(recruitmentPost.id.eq(bookmark.recruitmentPost.id))
                .where(
                        isClosedEq(isClosed),
                        isNotDeleted(),
                        isWriter(manageType, userDomain, writer)
                );
        joinByManagementType(userDomain, manageType, query);

        return query;
    }

    static class managementJpaUtils {
        static <T> void joinByManagementType(User userDomain, ManageType manageType, JPAQuery<T> query) {
            if (manageType.equals(ManageType.BOOKMARKED)) {
                query.leftJoin(bookmark).on(recruitmentPost.id.eq(bookmark.recruitmentPost.id))
                        .where(bookmark.user.id.eq(userDomain.getId()));
            } else if (manageType.equals(ManageType.APPLIED)) {
                query.leftJoin(recruitmentApplicant)
                        .on(recruitmentPost.id.eq(recruitmentApplicant.recruitmentPost.id))
                        .where(recruitmentApplicant.applicant.id.eq(userDomain.getId()),
                                recruitmentApplicant.deleteStatus.ne(DeleteStatus.DELETED));
            }
        }
    }

}
