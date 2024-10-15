package synk.meeteam.domain.recruitment.recruitment_post.repository;

import static synk.meeteam.domain.common.course.entity.QCourse.course;
import static synk.meeteam.domain.common.course.entity.QProfessor.professor;
import static synk.meeteam.domain.common.field.entity.QField.field;
import static synk.meeteam.domain.common.role.entity.QRole.role;
import static synk.meeteam.domain.common.skill.entity.QSkill.skill;
import static synk.meeteam.domain.common.tag.entity.QTag.tag;
import static synk.meeteam.domain.recruitment.recruitment_post.entity.QRecruitmentPost.recruitmentPost;
import static synk.meeteam.domain.recruitment.recruitment_post.repository.expression.RecruitmentExpressionUtils.categoryEq;
import static synk.meeteam.domain.recruitment.recruitment_post.repository.expression.RecruitmentExpressionUtils.isBookmark;
import static synk.meeteam.domain.recruitment.recruitment_post.repository.expression.RecruitmentExpressionUtils.scopeEq;
import static synk.meeteam.domain.recruitment.recruitment_post.repository.expression.RecruitmentExpressionUtils.titleContains;
import static synk.meeteam.domain.recruitment.recruitment_post.repository.expression.RecruitmentExpressionUtils.writerUniversityEqUser;
import static synk.meeteam.domain.recruitment.recruitment_role.entity.QRecruitmentRole.recruitmentRole;
import static synk.meeteam.domain.recruitment.recruitment_role_skill.entity.QRecruitmentRoleSkill.recruitmentRoleSkill;
import static synk.meeteam.domain.recruitment.recruitment_tag.entity.QRecruitmentTag.recruitmentTag;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import synk.meeteam.domain.common.tag.entity.TagType;
import synk.meeteam.domain.recruitment.recruitment_post.dto.SearchCondition;
import synk.meeteam.domain.recruitment.recruitment_post.repository.vo.QRecruitmentPostVo;
import synk.meeteam.domain.recruitment.recruitment_post.repository.vo.RecruitmentPostVo;
import synk.meeteam.domain.user.user.entity.QUser;
import synk.meeteam.domain.user.user.entity.User;
import synk.meeteam.global.entity.DeleteStatus;
import synk.meeteam.global.entity.Scope;

@Repository
@RequiredArgsConstructor
public class RecruitmentPostSearchRepositoryImpl implements RecruitmentPostSearchRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<RecruitmentPostVo> findBySearchConditionAndKeyword(
            Pageable pageable, SearchCondition condition, String keyword, User userDomain) {

        List<RecruitmentPostVo> contents = getPostVos(pageable, condition, keyword, userDomain);
        JPAQuery<Long> countQuery = getCount(condition, keyword, userDomain);

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    private List<RecruitmentPostVo> getPostVos(Pageable pageable, SearchCondition condition,
                                               String keyword, User userDomain) {
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
                                isBookmark(userDomain),
                                recruitmentPost.createdAt,
                                recruitmentPost.isClosed,
                                writer.imgVersion
                        )
                )
                .distinct()
                .from(recruitmentPost)
                .leftJoin(writer).on(recruitmentPost.createdBy.eq(writer.id))
                .where(
                        scopeEq(condition.getScope()),
                        userDomain != null ? writerUniversityEqUser(writer, userDomain, condition.getScope()) : null,
                        categoryEq(condition.getCategory()),
                        titleContains(keyword),
                        recruitmentPost.deleteStatus.ne(DeleteStatus.DELETED),
                        recruitmentPost.isClosed.isFalse()
                );

        searchJpaUtils.joinWithFieldAndTagAndRoleAndSkill(query, condition);

        //교내
        if (condition.getScope() == Scope.ON_CAMPUS) {
            searchJpaUtils.joinWithCourseAndProfessor(query, condition, userDomain);
        }

        return query.orderBy(recruitmentPost.createdAt.desc(), recruitmentPost.id.desc())
                .offset(pageable.getOffset()) //페이지 번호
                .limit(pageable.getPageSize()) //페이지 사이즈
                .fetch();
    }

    private JPAQuery<Long> getCount(SearchCondition condition, String keyword, User userDomain) {
        QUser writer = new QUser("writer");

        JPAQuery<Long> countQuery = queryFactory.select(recruitmentPost.countDistinct())
                .from(recruitmentPost)
                .leftJoin(writer).on(recruitmentPost.createdBy.eq(writer.id))
                .where(
                        scopeEq(condition.getScope()),
                        userDomain != null ? writerUniversityEqUser(writer, userDomain, condition.getScope()) : null,
                        categoryEq(condition.getCategory()),
                        titleContains(keyword),
                        recruitmentPost.deleteStatus.ne(DeleteStatus.DELETED),
                        recruitmentPost.isClosed.isFalse()
                );

        searchJpaUtils.joinWithFieldAndTagAndRoleAndSkill(countQuery, condition);
        //교내
        if (condition.getScope() == Scope.ON_CAMPUS) {
            searchJpaUtils.joinWithCourseAndProfessor(countQuery, condition, userDomain);
        }

        return countQuery;
    }


    static class searchJpaUtils {
        public static <T> void joinWithFieldAndTagAndRoleAndSkill(JPAQuery<T> query, SearchCondition condition) {
            //분야
            if (condition.isExistField()) {
                query.leftJoin(recruitmentPost.field, field)
                        .where(field.id.eq(condition.getFieldId()));
            }

            //태그
            if (condition.isExistTags()) {
                query.leftJoin(recruitmentTag).on(recruitmentPost.id.eq(recruitmentTag.recruitmentPost.id))
                        .leftJoin(recruitmentTag.tag, tag)
                        .where(
                                tag.type.eq(TagType.MEETEAM),
                                tag.id.in(condition.getTagIds())
                        );
            }

            //역할
            if (condition.isExistRoles()) {
                query.leftJoin(recruitmentRole)
                        .on(recruitmentPost.id.eq(recruitmentRole.recruitmentPost.id))
                        .leftJoin(recruitmentRole.role, role)
                        .where(role.id.in(condition.getRoleIds()));
                //역할 + 스킬
                if (condition.isExistSkills()) {
                    query.leftJoin(recruitmentRoleSkill)
                            .on(recruitmentRole.id.eq(recruitmentRoleSkill.recruitmentRole.id))
                            .leftJoin(recruitmentRoleSkill.skill, skill)
                            .where(skill.id.in(condition.getSkillIds()));
                }
            } else if (condition.isExistSkills()) {
                query.leftJoin(recruitmentRole)
                        .on(recruitmentPost.id.eq(recruitmentRole.recruitmentPost.id))
                        .leftJoin(recruitmentRoleSkill)
                        .on(recruitmentRole.id.eq(recruitmentRoleSkill.recruitmentRole.id))
                        .leftJoin(recruitmentRoleSkill.skill, skill)
                        .where(skill.id.in(condition.getSkillIds()));
            }
        }

        public static <T> void joinWithCourseAndProfessor(JPAQuery<T> query,
                                                          SearchCondition condition, User userDomain) {
            if (condition.isExistCourse()) {
                query.leftJoin(recruitmentPost.course, course)
                        .where(
                                course.id.eq(condition.getCourseId()),
                                course.university.eq(userDomain.getUniversity())
                        );
            }
            if (condition.isExistProfessor()) {
                query.leftJoin(recruitmentPost.professor, professor)
                        .where(
                                professor.id.eq(condition.getProfessorId()),
                                professor.university.eq(userDomain.getUniversity())
                        );
            }
        }
    }
}
