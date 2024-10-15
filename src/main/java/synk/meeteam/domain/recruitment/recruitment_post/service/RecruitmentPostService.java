package synk.meeteam.domain.recruitment.recruitment_post.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import synk.meeteam.domain.recruitment.recruitment_post.dto.ManageType;
import synk.meeteam.domain.recruitment.recruitment_post.dto.SearchCondition;
import synk.meeteam.domain.recruitment.recruitment_post.dto.SimpleRecruitmentPostMapper;
import synk.meeteam.domain.recruitment.recruitment_post.dto.response.PaginationSearchPostResponseDto;
import synk.meeteam.domain.recruitment.recruitment_post.dto.response.SimpleRecruitmentPostDto;
import synk.meeteam.domain.recruitment.recruitment_post.entity.RecruitmentPost;
import synk.meeteam.domain.recruitment.recruitment_post.repository.RecruitmentPostRepository;
import synk.meeteam.domain.recruitment.recruitment_post.repository.vo.RecruitmentPostVo;
import synk.meeteam.domain.user.user.entity.User;
import synk.meeteam.global.dto.PageInfo;
import synk.meeteam.global.dto.PaginationDto;
import synk.meeteam.global.entity.Scope;
import synk.meeteam.global.util.Encryption;
import synk.meeteam.infra.aws.S3FilePath;
import synk.meeteam.infra.aws.service.CloudFrontService;

@Service
@RequiredArgsConstructor
public class RecruitmentPostService {

    private final RecruitmentPostRepository recruitmentPostRepository;
    private final SimpleRecruitmentPostMapper simpleRecruitmentPostMapper;
    private final CloudFrontService cloudFrontService;

    @Transactional
    public RecruitmentPost writeRecruitmentPost(RecruitmentPost recruitmentPost) {
        return recruitmentPostRepository.save(recruitmentPost);
    }

    @Transactional(readOnly = true)
    public RecruitmentPost getRecruitmentPost(final Long postId) {
        return recruitmentPostRepository.findByIdOrElseThrow(postId);
    }

    @Transactional
    public void deleteRecruitmentPost(final Long postId, final Long userId) {
        RecruitmentPost recruitmentPost = recruitmentPostRepository.findByIdOrElseThrow(postId);
        recruitmentPost.softDelete(userId);
    }

    @Transactional
    public void incrementApplicantCount(RecruitmentPost recruitmentPost) {
        recruitmentPost.incrementApplicantCount();
    }

    @Transactional
    public void decrementApplicantCount(RecruitmentPost recruitmentPost) {
        recruitmentPost.decrementApplicantCount();
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

    @Transactional
    public RecruitmentPost decrementBookmarkCount(RecruitmentPost recruitmentPost) {
        return recruitmentPost.decrementBookmarkCount();
    }

    @Transactional
    public RecruitmentPost modifyRecruitmentPost(RecruitmentPost dstRecruitmentPost,
                                                 RecruitmentPost srcRecruitmentPost, Long userId) {

        dstRecruitmentPost.updateRecruitmentPost(srcRecruitmentPost.getTitle(), srcRecruitmentPost.getContent(),
                srcRecruitmentPost.getScope(), srcRecruitmentPost.getCategory(), srcRecruitmentPost.getField(),
                srcRecruitmentPost.getProceedType(), srcRecruitmentPost.getProceedingStart(),
                srcRecruitmentPost.getProceedingEnd(), srcRecruitmentPost.getDeadline(),
                srcRecruitmentPost.getBookmarkCount(),
                srcRecruitmentPost.getKakaoLink(), srcRecruitmentPost.isClosed(), srcRecruitmentPost.getMeeteam(),
                srcRecruitmentPost.getApplicantCount(), srcRecruitmentPost.getResponseCount(), userId);

        return recruitmentPostRepository.save(dstRecruitmentPost);
    }

    @Transactional(readOnly = true)
    public PaginationSearchPostResponseDto searchWithPageRecruitmentPost(int size, int page, SearchCondition condition,
                                                                         String keyword, User user) {
        boolean isNotAuthenticated = user == null;

        if (isNotAuthenticated) {
            condition.setScope(Scope.OFF_CAMPUS);
            condition.setCourseId(null);
            condition.setProfessorId(null);
        }
        Page<RecruitmentPostVo> postVos = recruitmentPostRepository
                .findBySearchConditionAndKeyword(PageRequest.of(page - 1, size), condition, keyword, user);
        PageInfo pageInfo = new PageInfo(page, size, postVos.getTotalElements(), postVos.getTotalPages());
        List<SimpleRecruitmentPostDto> contents = postVos.stream()
                .map((postVo) -> {
                    String writerEncryptedId = Encryption.encryptLong(postVo.getWriterId());
                    String imageUrl = cloudFrontService.getSignedUrl(S3FilePath.USER, postVo.getWriterProfileImg(),
                            postVo.getWriterVersion());
                    return simpleRecruitmentPostMapper.toSimpleRecruitmentPostDto(postVo, writerEncryptedId, imageUrl);
                }).toList();

        return new PaginationSearchPostResponseDto(contents, pageInfo);
    }

    @Transactional
    public RecruitmentPost setLink(Long postId, String link, Long userId) {

        RecruitmentPost recruitmentPost = recruitmentPostRepository.findByIdOrElseThrow(postId);

        recruitmentPost.setLink(link, userId);

        return recruitmentPost;
    }

    @Transactional
    public void incrementResponseCount(Long postId, Long userId, long responseCount) {
        RecruitmentPost recruitmentPost = recruitmentPostRepository.findByIdOrElseThrow(postId);
        recruitmentPost.incrementResponseCount(userId, responseCount);
    }

    @Transactional
    public PaginationDto<SimpleRecruitmentPostDto> getManagementPost(int size, int page, User user, Boolean isClosed,
                                                                     ManageType manageType) {
        Page<RecruitmentPostVo> postVos = recruitmentPostRepository.findManagementPost(PageRequest.of(page - 1, size),
                user, isClosed, manageType);
        PageInfo pageInfo = new PageInfo(page, size, postVos.getTotalElements(), postVos.getTotalPages());
        List<SimpleRecruitmentPostDto> contents = postVos.stream()
                .map((postVo) -> {
                    String writerEncryptedId = Encryption.encryptLong(postVo.getWriterId());
                    String imageUrl = cloudFrontService.getSignedUrl(S3FilePath.USER, postVo.getWriterProfileImg(),
                            postVo.getWriterVersion());
                    return simpleRecruitmentPostMapper.toSimpleRecruitmentPostDto(postVo, writerEncryptedId, imageUrl);
                }).toList();
        return new PaginationDto<>(contents, pageInfo);
    }
}
