package synk.meeteam.domain.recruitment.recruitment_comment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static synk.meeteam.domain.recruitment.recruitment_comment.exception.RecruitmentCommentExceptionType.INVALID_COMMENT;
import static synk.meeteam.domain.recruitment.recruitment_comment.exception.RecruitmentCommentExceptionType.INVALID_USER;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import synk.meeteam.domain.recruitment.recruitment_comment.entity.RecruitmentComment;
import synk.meeteam.domain.recruitment.recruitment_comment.exception.RecruitmentCommentException;
import synk.meeteam.domain.recruitment.recruitment_comment.repository.RecruitmentCommentRepository;
import synk.meeteam.domain.recruitment.recruitment_comment.service.RecruitmentCommentService;
import synk.meeteam.domain.recruitment.recruitment_comment.service.vo.RecruitmentCommentVO;
import synk.meeteam.domain.recruitment.recruitment_post.RecruitmentPostFixture;
import synk.meeteam.domain.recruitment.recruitment_post.dto.response.GetCommentResponseDto;
import synk.meeteam.domain.recruitment.recruitment_post.entity.RecruitmentPost;
import synk.meeteam.infra.aws.service.CloudFrontService;

@ExtendWith(MockitoExtension.class)
public class RecruitmentCommentServiceTest {

    @InjectMocks
    private RecruitmentCommentService recruitmentCommentService;

    @Mock
    private RecruitmentCommentRepository recruitmentCommentRepository;

    @Mock
    private CloudFrontService cloudFrontService;

    @Test
    void 댓글조회_댓글그룹반환() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("제목입니당");
        recruitmentPost.setId(1L);
        recruitmentPost.setCreatedBy(1L);
        List<RecruitmentCommentVO> recruitmentComments = RecruitmentCommentFixture.createRecruitmentComments(
                recruitmentPost);
        doReturn(recruitmentComments).when(recruitmentCommentRepository)
                .findAllByRecruitmentId(recruitmentPost.getId());

        doReturn("임시 url").when(cloudFrontService).getSignedUrl(any(), any(), any());

        // when
        List<GetCommentResponseDto> getCommentResponseDtos = recruitmentCommentService.getRecruitmentComments(
                recruitmentPost);

        // then
        Assertions.assertThat(getCommentResponseDtos.get(0))
                .extracting("nickname", "isWriter")
                .containsExactly("닉네임입니다2", false);

        Assertions.assertThat(getCommentResponseDtos.get(0).replies().get(0))
                .extracting("nickname", "isWriter")
                .containsExactly("닉네임입니다", true);
        Assertions.assertThat(getCommentResponseDtos.get(0).replies().get(1))
                .extracting("nickname", "isWriter")
                .containsExactly("닉네임입니다3", false);

        Assertions.assertThat(getCommentResponseDtos.get(1))
                .extracting("nickname", "isWriter")
                .containsExactly("닉네임입니다3", false);

    }

    @Test
    void 댓글등록_성공_최초댓글경우() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("제목입니당");
        long groupNumber = 0L;
        long groupOrder = 1L;

        RecruitmentComment comment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(true)
                .recruitmentPost(recruitmentPost)
                .groupNumber(groupNumber)
                .groupOrder(groupOrder)
                .build();

        doReturn(comment).when(recruitmentCommentRepository).save(any());
        doReturn(Optional.ofNullable(null)).when(recruitmentCommentRepository)
                .findFirstByRecruitmentPostOrderByGroupNumberDesc(recruitmentPost);

        // when
        RecruitmentComment savedRecruitmentComment = recruitmentCommentService.registerRecruitmentComment(comment);

        // then
        Assertions.assertThat(savedRecruitmentComment)
                .extracting("content", "isParent", "groupNumber", "groupOrder")
                .containsExactly("저 하고 싶어요", true, groupNumber + 1, groupOrder);
    }

    @Test
    void 댓글등록_성공_이전댓글있는경우() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("제목입니당");
        long prevGroupNumber = 1L;
        long prevGroupOrder = 1L;

        long groupNumber = 0L;
        long groupOrder = 1L;

        RecruitmentComment prevComment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(true)
                .recruitmentPost(recruitmentPost)
                .groupNumber(prevGroupNumber)
                .groupOrder(prevGroupOrder)
                .build();

        RecruitmentComment comment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(true)
                .recruitmentPost(recruitmentPost)
                .groupNumber(groupNumber)
                .groupOrder(groupOrder)
                .build();

        doReturn(comment).when(recruitmentCommentRepository).save(any());
        doReturn(Optional.ofNullable(prevComment)).when(recruitmentCommentRepository)
                .findFirstByRecruitmentPostOrderByGroupNumberDesc(recruitmentPost);

        // when
        RecruitmentComment savedRecruitmentComment = recruitmentCommentService.registerRecruitmentComment(comment);

        // then
        Assertions.assertThat(savedRecruitmentComment)
                .extracting("content", "isParent", "groupNumber", "groupOrder")
                .containsExactly("저 하고 싶어요", true, prevGroupNumber + 1, groupOrder);
    }

    @Test
    void 대댓글등록_성공() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("제목입니당");
        long parentGroupNumber = 1L;
        long parentGroupOrder = 1L;

        long groupNumber = 1L;
        long groupOrder = 2L;

        RecruitmentComment parentComment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(true)
                .recruitmentPost(recruitmentPost)
                .groupNumber(parentGroupNumber)
                .groupOrder(parentGroupOrder)
                .build();

        RecruitmentComment childComment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(false)
                .recruitmentPost(recruitmentPost)
                .groupNumber(groupNumber)
                .groupOrder(groupOrder)
                .build();

        doReturn(childComment).when(recruitmentCommentRepository).save(any());
        doReturn(parentComment).when(recruitmentCommentRepository)
                .findLatestGroupOrderOrElseThrow(recruitmentPost, groupNumber);
        doReturn(Optional.of(parentComment)).when(recruitmentCommentRepository)
                .findFirstByRecruitmentPostAndGroupNumberOrderByGroupOrderDesc(any(), anyLong());

        // when
        RecruitmentComment savedRecruitmentComment = recruitmentCommentService.registerRecruitmentComment(childComment);

        // then
        Assertions.assertThat(savedRecruitmentComment)
                .extracting("content", "isParent", "groupNumber", "groupOrder")
                .containsExactly("저 하고 싶어요", false, groupNumber, groupOrder);
    }

    @ParameterizedTest
    @CsvSource(value = {"1", "2"})
    void 댓글등록_예외발생_groupNumber가잘못된경우(long groupNumber) {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("제목입니당");

        RecruitmentComment comment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(true)
                .recruitmentPost(recruitmentPost)
                .groupNumber(groupNumber)
                .build();

        // when, then
        Assertions.assertThatThrownBy(() -> recruitmentCommentService.registerRecruitmentComment(comment))
                .isInstanceOf(RecruitmentCommentException.class)
                .hasMessageContaining(INVALID_COMMENT.message());
    }


    @ParameterizedTest
    @CsvSource(value = {"1", "3"})
    void 대댓글등록_예외발생_대댓글의groupNumber가잘못된경우(long groupNumber) {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("제목입니당");
        long parentGroupNumber = 2L;
        long parentGroupOrder = 2L;

        RecruitmentComment parentComment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(true)
                .recruitmentPost(recruitmentPost)
                .groupNumber(parentGroupNumber)
                .groupOrder(parentGroupOrder)
                .build();

        RecruitmentComment childComment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(false)
                .recruitmentPost(recruitmentPost)
                .groupNumber(groupNumber)
                .build();

        doReturn(parentComment).when(recruitmentCommentRepository)
                .findLatestGroupOrderOrElseThrow(recruitmentPost, groupNumber);

        // when, then
        Assertions.assertThatThrownBy(() -> recruitmentCommentService.registerRecruitmentComment(childComment))
                .isInstanceOf(RecruitmentCommentException.class)
                .hasMessageContaining(INVALID_COMMENT.message());
    }

    @Test
    void 대댓글등록_예외발생_존재하지않은groupNumer경우() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("제목입니당");
        long parentGroupNumber = 2L;
        long parentGroupOrder = 2L;

        long groupNumber = 5L;
        long groupOrder = 2L;

        RecruitmentComment parentComment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(true)
                .recruitmentPost(recruitmentPost)
                .groupNumber(parentGroupNumber)
                .groupOrder(parentGroupOrder)
                .build();

        RecruitmentComment childComment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(false)
                .recruitmentPost(recruitmentPost)
                .groupNumber(groupNumber)
                .groupOrder(groupOrder)
                .build();

        doReturn(parentComment).when(recruitmentCommentRepository)
                .findLatestGroupOrderOrElseThrow(recruitmentPost, groupNumber);

        // when, then
        Assertions.assertThatThrownBy(() -> recruitmentCommentService.registerRecruitmentComment(childComment))
                .isInstanceOf(RecruitmentCommentException.class)
                .hasMessageContaining(INVALID_COMMENT.message());
    }

    @Test
    void 댓글삭제_성공_대댓글이있는경우() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상 제목입니다.");
        Long userId = 1L;
        Long commentId = 1L;
        long groupNumber = 1;
        long groupOrder = 1;

        RecruitmentComment parentComment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(true)
                .recruitmentPost(recruitmentPost)
                .groupNumber(groupNumber)
                .groupOrder(groupOrder)
                .build();

        RecruitmentComment childComment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(false)
                .recruitmentPost(recruitmentPost)
                .groupNumber(groupNumber)
                .groupOrder(groupOrder + 1)
                .build();

        parentComment.setCreatedBy(userId);
        doReturn(parentComment).when(recruitmentCommentRepository).findByIdOrElseThrow(any());
        doReturn(childComment).when(recruitmentCommentRepository).findLatestGroupOrderOrElseThrow(any(), anyLong());

        // when, then
        recruitmentCommentService.deleteComment(commentId, userId, recruitmentPost);
    }

    @Test
    void 댓글삭제_예외발생_존재하지않은댓글경우() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상 제목입니다.");
        Long userId = 1L;
        Long commentId = 1L;
        long groupNumber = 1;
        long groupOrder = 1;

        RecruitmentComment parentComment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(true)
                .recruitmentPost(recruitmentPost)
                .groupNumber(groupNumber)
                .groupOrder(groupOrder)
                .build();

        parentComment.setCreatedBy(userId);
        doThrow(new RecruitmentCommentException(INVALID_COMMENT)).when(recruitmentCommentRepository)
                .findByIdOrElseThrow(any());

        // when, then
        Assertions.assertThatThrownBy(() -> recruitmentCommentService.deleteComment(commentId, userId, recruitmentPost))
                .isInstanceOf(RecruitmentCommentException.class)
                .hasMessageContaining(INVALID_COMMENT.message());

    }

    @Test
    void 댓글삭제_예외발생_댓글작성자가아닌경우() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상 제목입니다.");
        Long userId = 1L;
        Long commentId = 1L;
        long groupNumber = 1;
        long groupOrder = 1;

        RecruitmentComment parentComment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(true)
                .recruitmentPost(recruitmentPost)
                .groupNumber(groupNumber)
                .groupOrder(groupOrder)
                .build();

        parentComment.setCreatedBy(0L);
        doReturn(parentComment).when(recruitmentCommentRepository).findByIdOrElseThrow(any());

        // when, then
        Assertions.assertThatThrownBy(() -> recruitmentCommentService.deleteComment(commentId, userId, recruitmentPost))
                .isInstanceOf(RecruitmentCommentException.class)
                .hasMessageContaining(INVALID_USER.message());

    }

    @Test
    void 댓글수정_성공() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상 제목입니다.");
        Long userId = 1L;
        Long commentId = 1L;
        long groupNumber = 1;
        long groupOrder = 1;

        RecruitmentComment comment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(true)
                .recruitmentPost(recruitmentPost)
                .groupNumber(groupNumber)
                .groupOrder(groupOrder)
                .build();

        comment.setCreatedBy(userId);

        doReturn(comment).when(recruitmentCommentRepository).findByIdOrElseThrow(any());

        // when, then
        recruitmentCommentService.modifyRecruitmentComment(userId, commentId, "수정내용입니다.");
    }

    @Test
    void 댓글수정_예외발생_작성자가아닌경우() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상 제목입니다.");
        Long userId = 1L;
        Long commentId = 1L;
        long groupNumber = 1;
        long groupOrder = 1;

        RecruitmentComment comment = RecruitmentComment.builder()
                .content("저 하고 싶어요")
                .isParent(true)
                .recruitmentPost(recruitmentPost)
                .groupNumber(groupNumber)
                .groupOrder(groupOrder)
                .build();

        comment.setCreatedBy(0L);

        doReturn(comment).when(recruitmentCommentRepository).findByIdOrElseThrow(any());

        // when, then
        Assertions.assertThatThrownBy(
                        () -> recruitmentCommentService.modifyRecruitmentComment(userId, commentId, "수정내용입니다."))
                .isInstanceOf(RecruitmentCommentException.class)
                .hasMessageContaining(INVALID_USER.message());
    }

}
