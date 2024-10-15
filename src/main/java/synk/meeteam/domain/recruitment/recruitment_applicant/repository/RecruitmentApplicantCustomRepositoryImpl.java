package synk.meeteam.domain.recruitment.recruitment_applicant.repository;

import static synk.meeteam.domain.common.role.entity.QRole.role;
import static synk.meeteam.domain.common.university.entity.QUniversity.university;
import static synk.meeteam.domain.recruitment.recruitment_applicant.entity.QRecruitmentApplicant.recruitmentApplicant;
import static synk.meeteam.domain.recruitment.recruitment_post.entity.QRecruitmentPost.recruitmentPost;
import static synk.meeteam.domain.user.user.entity.QUser.user;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import synk.meeteam.domain.common.department.entity.QDepartment;
import synk.meeteam.domain.recruitment.recruitment_applicant.dto.response.GetApplicantDto;
import synk.meeteam.domain.recruitment.recruitment_applicant.dto.response.QGetApplicantDto;
import synk.meeteam.domain.recruitment.recruitment_applicant.entity.RecruitStatus;
import synk.meeteam.global.entity.DeleteStatus;

@Repository
@RequiredArgsConstructor
public class RecruitmentApplicantCustomRepositoryImpl implements RecruitmentApplicantCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public long updateRecruitStatus(List<Long> applicantIds, RecruitStatus recruitStatus) {
        return jpaQueryFactory.update(recruitmentApplicant)
                .where(recruitmentApplicant.id.in(applicantIds))
                .set(recruitmentApplicant.recruitStatus, recruitStatus)
                .execute();
    }

    @Override
    public Slice<GetApplicantDto> findByPostIdAndRoleId(Long postId, Long roleId, Pageable pageable) {

        Predicate isUniversityMainEmail = user.isUniversityMainEmail.eq(true);

        StringExpression getMainMail = new CaseBuilder()
                .when(isUniversityMainEmail)
                .then(recruitmentApplicant.applicant.universityEmail)
                .otherwise(recruitmentApplicant.applicant.subEmail);

        List<GetApplicantDto> contents = jpaQueryFactory
                .select(new QGetApplicantDto(recruitmentApplicant.id,
                        recruitmentApplicant.applicant.id.stringValue(),
                        recruitmentApplicant.applicant.nickname, recruitmentApplicant.applicant.profileImgFileName,
                        recruitmentApplicant.applicant.name, recruitmentApplicant.applicant.gpa,
                        recruitmentApplicant.applicant.university.name, recruitmentApplicant.applicant.department.name,
                        getMainMail, recruitmentApplicant.applicant.admissionYear,
                        recruitmentApplicant.role.name, recruitmentApplicant.comment,
                        recruitmentApplicant.applicant.imgVersion))
                .from(recruitmentApplicant)
                .leftJoin(recruitmentApplicant.applicant, user)
                .leftJoin(recruitmentApplicant.recruitmentPost, recruitmentPost)
                .leftJoin(recruitmentApplicant.role, role)
                .leftJoin(recruitmentApplicant.applicant.university, university)
                .leftJoin(recruitmentApplicant.applicant.department, QDepartment.department)
                .where(eqRole(roleId),
                        recruitmentApplicant.recruitmentPost.id.eq(postId),
                        recruitmentApplicant.recruitStatus.eq(RecruitStatus.NONE),
                        recruitmentApplicant.deleteStatus.eq(DeleteStatus.ALIVE))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return new SliceImpl<>(contents, pageable, hasNextPage(contents, pageable.getPageSize()));
    }

    private BooleanExpression eqRole(Long roleId) {
        if (roleId == null) {
            return null;
        }
        return recruitmentApplicant.role.id.eq(roleId);
    }

    private boolean hasNextPage(List<GetApplicantDto> contents, int pageSize) {
        if (contents.size() > pageSize) {
            contents.remove(pageSize);
            return true;
        }
        return false;
    }
}
