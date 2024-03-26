package synk.meeteam.domain.recruitment.recruitment_post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import synk.meeteam.domain.recruitment.recruitment_post.entity.RecruitmentPost;
import synk.meeteam.domain.recruitment.recruitment_post.repository.RecruitmentPostRepository;

@Service
@RequiredArgsConstructor
public class RecruitmentPostService {

    private final RecruitmentPostRepository recruitmentPostRepository;

    @Transactional
    public RecruitmentPost writeRecruitmentPost(RecruitmentPost recruitmentPost) {
        return recruitmentPostRepository.save(recruitmentPost);
    }

    @Transactional(readOnly = true)
    public RecruitmentPost getRecruitmentPost(final Long postId) {
        return recruitmentPostRepository.findByIdOrElseThrow(postId);
    }

    @Transactional
    public void incrementApplicantCount(RecruitmentPost recruitmentPost) {
        recruitmentPost.addApplicantCount();
    }

    @Transactional
    public RecruitmentPost closeRecruitment(Long postId, Long userId) {
        RecruitmentPost recruitmentPost = getRecruitmentPost(postId);
        recruitmentPost.closeRecruitmentPost(userId);

        return recruitmentPost;

    }

    @Transactional
    public RecruitmentPost incrementBookmarkCount(RecruitmentPost recruitmentPost) {
        return recruitmentPost.incrementBookmarkCount();
    }
}
