package synk.meeteam.domain.recruitment.recruitment_comment.repository;

import static synk.meeteam.domain.recruitment.recruitment_comment.entity.QRecruitmentComment.recruitmentComment;
import static synk.meeteam.domain.user.user.entity.QUser.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import synk.meeteam.domain.recruitment.recruitment_comment.service.vo.QRecruitmentCommentVO;
import synk.meeteam.domain.recruitment.recruitment_comment.service.vo.RecruitmentCommentVO;

@Repository
@RequiredArgsConstructor
public class RecruitmentCommentRepositoryCustomImpl implements RecruitmentCommentRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<RecruitmentCommentVO> findAllByRecruitmentId(Long postId) {
        return queryFactory
                .select(new QRecruitmentCommentVO(recruitmentComment.id, user.id,
                        user.nickname,
                        user.profileImgFileName, recruitmentComment.content, recruitmentComment.createdAt,
                        recruitmentComment.isParent, recruitmentComment.groupNumber, recruitmentComment.groupOrder,
                        recruitmentComment.isDeleted, user.imgVersion))
                .from(recruitmentComment)
                .leftJoin(user)
                .on(recruitmentComment.createdBy.eq(user.id))
                .where(recruitmentComment.recruitmentPost.id.eq(postId))
                .orderBy(recruitmentComment.groupNumber.asc(), recruitmentComment.createdAt.asc())
                .fetch();
    }
}
