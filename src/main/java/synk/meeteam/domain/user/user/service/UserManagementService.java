package synk.meeteam.domain.user.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import synk.meeteam.domain.portfolio.portfolio.entity.Portfolio;
import synk.meeteam.domain.portfolio.portfolio.repository.PortfolioRepository;
import synk.meeteam.domain.portfolio.portfolio_link.repository.PortfolioLinkRepository;
import synk.meeteam.domain.portfolio.portfolio_skill.repository.PortfolioSkillRepository;
import synk.meeteam.domain.recruitment.bookmark.repository.BookmarkRepository;
import synk.meeteam.domain.recruitment.recruitment_applicant.repository.RecruitmentApplicantRepository;
import synk.meeteam.domain.recruitment.recruitment_comment.repository.RecruitmentCommentRepository;
import synk.meeteam.domain.recruitment.recruitment_post.entity.RecruitmentPost;
import synk.meeteam.domain.recruitment.recruitment_post.repository.RecruitmentPostRepository;
import synk.meeteam.domain.recruitment.recruitment_role.repository.RecruitmentRoleRepository;
import synk.meeteam.domain.recruitment.recruitment_role_skill.repository.RecruitmentRoleSkillRepository;
import synk.meeteam.domain.recruitment.recruitment_tag.repository.RecruitmentTagRepository;
import synk.meeteam.domain.user.award.repository.AwardRepository;
import synk.meeteam.domain.user.user.entity.User;
import synk.meeteam.domain.user.user.repository.UserRepository;
import synk.meeteam.domain.user.user_link.repository.UserLinkRepository;
import synk.meeteam.domain.user.user_skill.repository.UserSkillRepository;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    // 유저 관련
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserLinkRepository userLinkRepository;
    private final AwardRepository awardRepository;

    // 포토폴리오 관련
    private final PortfolioRepository portfolioRepository;
    private final PortfolioSkillRepository portfolioSkillRepository;
    private final PortfolioLinkRepository portfolioLinkRepository;

    // 구인 관련
    private final RecruitmentPostRepository recruitmentPostRepository;
    private final RecruitmentRoleRepository recruitmentRoleRepository;
    private final RecruitmentTagRepository recruitmentTagRepository;
    private final RecruitmentCommentRepository recruitmentCommentRepository;
    private final RecruitmentApplicantRepository recruitmentApplicantRepository;

    // 기타
    private final BookmarkRepository bookmarkRepository;

    @Transactional
    public void deleteUser(User user) {
        List<Long> postIds = recruitmentPostRepository.findAllByCreatedBy(user.getId()).stream()
                .map(RecruitmentPost::getId).toList();
        if(!postIds.isEmpty()) {
            deleteRecruitmentPosts(postIds);
        }

        List<Long> portfolioIds = portfolioRepository.findAllByCreatedBy(user.getId())
                .stream().map(Portfolio::getId).toList();
		if(!portfolioIds.isEmpty()){
			deletePortfolios(portfolioIds);
		}

        deleteProfile(user.getId());
        deleteRelatedUserData(user.getId());

        userRepository.delete(user);
    }

    private void deleteRecruitmentPosts(List<Long> postIds) {
        bookmarkRepository.deleteAllByPostIdInQuery(postIds);
        recruitmentRoleRepository.deleteAllByPostIdInQuery(postIds);
        recruitmentTagRepository.deleteAllByPostIdInQuery(postIds);
        recruitmentCommentRepository.deleteAllByPostIdInQuery(postIds);
        recruitmentApplicantRepository.deleteAllByPostIdInQuery(postIds);
        recruitmentPostRepository.deleteAllByIdInQuery(postIds);
    }

    private void deletePortfolios(List<Long> portfolioIds) {
        portfolioSkillRepository.deleteAllByPortfolioIdsInQuery(portfolioIds);
        portfolioLinkRepository.deleteAllByPortfolioIdsInQuery(portfolioIds);
        portfolioRepository.deleteAllByIdsInQuery(portfolioIds);
    }

    private void deleteProfile(Long userId) {
        userSkillRepository.deleteAllByUserId(userId);
        userLinkRepository.deleteAllByUserId(userId);
        awardRepository.deleteAllByUserId(userId);
    }

    private void deleteRelatedUserData(Long userId) {
        bookmarkRepository.deleteAllByUserId(userId);
        recruitmentApplicantRepository.deleteAllByUserId(userId);
    }
}
