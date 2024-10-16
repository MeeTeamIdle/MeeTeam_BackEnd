package synk.meeteam.domain.recruitment.recruitment_comment.service;

import static synk.meeteam.domain.recruitment.recruitment_comment.exception.RecruitmentCommentExceptionType.INVALID_COMMENT;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import synk.meeteam.domain.recruitment.recruitment_comment.entity.RecruitmentComment;
import synk.meeteam.domain.recruitment.recruitment_comment.exception.RecruitmentCommentException;
import synk.meeteam.domain.recruitment.recruitment_comment.repository.RecruitmentCommentRepository;
import synk.meeteam.domain.recruitment.recruitment_comment.service.vo.RecruitmentCommentVO;
import synk.meeteam.domain.recruitment.recruitment_post.dto.response.GetCommentResponseDto;
import synk.meeteam.domain.recruitment.recruitment_post.dto.response.GetReplyResponseDto;
import synk.meeteam.domain.recruitment.recruitment_post.entity.RecruitmentPost;
import synk.meeteam.global.util.Encryption;
import synk.meeteam.infra.aws.S3FilePath;
import synk.meeteam.infra.aws.service.CloudFrontService;

@Service
@RequiredArgsConstructor
public class RecruitmentCommentService {

    private final RecruitmentCommentRepository recruitmentCommentRepository;
    private final CloudFrontService cloudFrontService;

    // 가공된 형태를 많이 사용할 것 같다.
    // 그래서 Dto를 바로 반환하는 식으로 만들었다.
    public List<GetCommentResponseDto> getRecruitmentComments(RecruitmentPost recruitmentPost) {
        List<RecruitmentCommentVO> commentVOs = recruitmentCommentRepository.findAllByRecruitmentId(
                recruitmentPost.getId());

        List<GetCommentResponseDto> groupedComments = new ArrayList<>();
        Long writerId = recruitmentPost.getCreatedBy();

        for (RecruitmentCommentVO comment : commentVOs) {
            boolean isWriter = writerId.equals(comment.getUserId());
            String profileImg = cloudFrontService.getSignedUrl(S3FilePath.USER, comment.getProfileImg(),
                    comment.getUserVersion());
            String commentWriterId = Encryption.encryptLong(comment.getUserId());

            if (comment.isParent()) {
                List<GetReplyResponseDto> replies = new ArrayList<>();
                groupedComments.add(
                        new GetCommentResponseDto(comment.getId(), commentWriterId, comment.getNickname(), profileImg,
                                comment.getContent(), comment.getCreateAt(), isWriter, comment.getGroupNumber(),
                                comment.getGroupOrder(), replies));
                continue;
            }

            groupedComments.get(groupedComments.size() - 1).replies()
                    .add(new GetReplyResponseDto(comment.getId(), Encryption.encryptLong(comment.getUserId()),
                            comment.getNickname(), profileImg, comment.getContent(), comment.getCreateAt(),
                            isWriter, comment.getGroupOrder()));
        }

        return groupedComments;
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId, RecruitmentPost recruitmentPost) {
        RecruitmentComment recruitmentComment = recruitmentCommentRepository.findByIdOrElseThrow(commentId);

        recruitmentComment.validateWriter(userId);

        RecruitmentComment latestRecruitmentComment = recruitmentCommentRepository.findLatestGroupOrderOrElseThrow(
                recruitmentPost, recruitmentComment.getGroupNumber());

        if (recruitmentComment.hasChildComment(latestRecruitmentComment.getGroupOrder())) {
            recruitmentComment.softDelete(userId);
            return;
        }

        recruitmentCommentRepository.delete(recruitmentComment);
    }

    @Transactional
    public RecruitmentComment registerRecruitmentComment(RecruitmentComment recruitmentComment) {
        validateComment(recruitmentComment);

        long groupNumber = recruitmentComment.getGroupNumber();
        if (groupNumber == 0) {  // 댓글인 경우
            groupNumber = getNewGroupNumber(recruitmentComment.getRecruitmentPost());
        }

        long groupOrder = getNewGroupOrder(recruitmentComment.getRecruitmentPost(), groupNumber);

        recruitmentComment.updateGroupNumberAndGroupOrder(groupNumber, groupOrder);

        return recruitmentCommentRepository.save(recruitmentComment);
    }

    @Transactional
    public void modifyRecruitmentComment(Long userId, Long commentId, String content) {
        RecruitmentComment recruitmentComment = recruitmentCommentRepository.findByIdOrElseThrow(commentId);

        recruitmentComment.modifyContent(content, userId);
    }

    private long getNewGroupNumber(RecruitmentPost recruitmentPost) {
        RecruitmentComment recruitmentComment = recruitmentCommentRepository.findFirstByRecruitmentPostOrderByGroupNumberDesc(
                recruitmentPost).orElse(null);

        if (recruitmentComment == null) {
            return 1;
        }

        return recruitmentComment.getGroupNumber() + 1;
    }

    private long getNewGroupOrder(RecruitmentPost recruitmentPost, long groupNumber) {
        RecruitmentComment recruitmentComment = recruitmentCommentRepository.findFirstByRecruitmentPostAndGroupNumberOrderByGroupOrderDesc(
                recruitmentPost, groupNumber).orElse(null);

        if (recruitmentComment == null) {
            return 1;
        }

        return recruitmentComment.getGroupOrder() + 1;
    }

    private void validateComment(RecruitmentComment recruitmentComment) {
        long validGroupNumber = 0;

        if (!recruitmentComment.isParent()) {
            // 대댓글인 경우
            RecruitmentComment foundRecruitmentComment = recruitmentCommentRepository.findLatestGroupOrderOrElseThrow(
                    recruitmentComment.getRecruitmentPost(), recruitmentComment.getGroupNumber());
            validGroupNumber = foundRecruitmentComment.getGroupNumber();
        }

        if (validGroupNumber != recruitmentComment.getGroupNumber()) {
            throw new RecruitmentCommentException(INVALID_COMMENT);
        }
    }
}
