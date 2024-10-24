package synk.meeteam.domain.recruitment.recruitment_applicant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static synk.meeteam.domain.recruitment.recruitment_applicant.exception.RecruitmentApplicantExceptionType.ALREADY_PROCESSED_APPLICANT;
import static synk.meeteam.domain.recruitment.recruitment_applicant.exception.RecruitmentApplicantExceptionType.INVALID_REQUEST;
import static synk.meeteam.domain.recruitment.recruitment_applicant.exception.RecruitmentApplicantExceptionType.INVALID_USER;
import static synk.meeteam.domain.recruitment.recruitment_applicant.exception.RecruitmentApplicantExceptionType.SS_602;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.ActiveProfiles;
import synk.meeteam.domain.common.role.RoleFixture;
import synk.meeteam.domain.common.role.entity.Role;
import synk.meeteam.domain.recruitment.recruitment_applicant.dto.response.GetApplicantDto;
import synk.meeteam.domain.recruitment.recruitment_applicant.dto.response.GetApplicantResponseDto;
import synk.meeteam.domain.recruitment.recruitment_applicant.entity.RecruitmentApplicant;
import synk.meeteam.domain.recruitment.recruitment_applicant.exception.RecruitmentApplicantException;
import synk.meeteam.domain.recruitment.recruitment_applicant.repository.RecruitmentApplicantRepository;
import synk.meeteam.domain.recruitment.recruitment_applicant.service.RecruitmentApplicantService;
import synk.meeteam.domain.recruitment.recruitment_post.RecruitmentPostFixture;
import synk.meeteam.domain.recruitment.recruitment_post.entity.RecruitmentPost;
import synk.meeteam.domain.user.user.UserFixture;
import synk.meeteam.domain.user.user.entity.User;
import synk.meeteam.global.util.Encryption;
import synk.meeteam.infra.aws.service.CloudFrontService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RecruitmentApplicantServiceTest {
    @InjectMocks
    private RecruitmentApplicantService recruitmentApplicantService;

    @Mock
    private RecruitmentApplicantRepository recruitmentApplicantRepository;

    @Mock
    private CloudFrontService cloudFrontService;

    @Test
    void 구인신청_성공() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상제목");
        User user = UserFixture.createUser();
        Role role = RoleFixture.createRole("백엔드개발자");
        RecruitmentApplicant recruitmentApplicant = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user, role);

        doReturn(recruitmentApplicant)
                .when(recruitmentApplicantRepository).save(recruitmentApplicant);

        // when
        recruitmentApplicantService.registerRecruitmentApplicant(recruitmentApplicant);

        // then
    }

    @Test
    void 구인신청_예외발생_이미신청한경우() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상제목");
        User user = UserFixture.createUser();
        Role role = RoleFixture.createRole("백엔드개발자");
        RecruitmentApplicant recruitmentApplicant = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user, role);

        doReturn(Optional.ofNullable(recruitmentApplicant))
                .when(recruitmentApplicantRepository)
                .findByRecruitmentPostAndApplicantAndDeleteStatus(any(), any(), any());

        // when, then
        Assertions.assertThatThrownBy(
                        () -> recruitmentApplicantService.registerRecruitmentApplicant(recruitmentApplicant))
                .isInstanceOf(RecruitmentApplicantException.class)
                .hasMessageContaining(SS_602.message());

        // then
    }

    @Test
    void 구인신청취소_성공() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상제목");
        User user = UserFixture.createUser();
        Role role = RoleFixture.createRole("백엔드개발자");
        RecruitmentApplicant recruitmentApplicant = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user, role);

        // when, then
        recruitmentApplicantService.cancelRegisterRecruitmentApplicant(recruitmentApplicant);
    }


    @Test
    void 역할id목록조회_성공() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상제목");
        User user1 = UserFixture.createUser();
        Role role1 = RoleFixture.createRole("백엔드개발자");
        role1.setId(1L);
        User user2 = UserFixture.createUser();
        Role role2 = RoleFixture.createRole("백엔드개발자");
        role2.setId(2L);
        RecruitmentApplicant recruitmentApplicant1 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user1, role1);
        RecruitmentApplicant recruitmentApplicant2 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user2, role2);

        // when
        List<Long> roleIds = recruitmentApplicantService.getRoleIds(
                List.of(recruitmentApplicant1, recruitmentApplicant2));

        // then
        Assertions.assertThat(roleIds.get(0)).isEqualTo(1L);
        Assertions.assertThat(roleIds.get(1)).isEqualTo(2L);

    }

    @Test
    void 역할id목록조회_예외발생_role이null일경우() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상제목");
        User user1 = UserFixture.createUser();
        User user2 = UserFixture.createUser();
        RecruitmentApplicant recruitmentApplicant1 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user1, null);
        RecruitmentApplicant recruitmentApplicant2 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user2, null);

        // when
        Assertions.assertThatThrownBy(
                        () -> recruitmentApplicantService.getRoleIds(List.of(recruitmentApplicant1, recruitmentApplicant2)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 구인된역할수조회_성공() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상제목");
        User user1 = UserFixture.createUser();
        Role role1 = RoleFixture.createRole("백엔드개발자");
        role1.setId(1L);
        User user2 = UserFixture.createUser();
        Role role2 = RoleFixture.createRole("프론트엔드개발자");
        role2.setId(2L);
        RecruitmentApplicant recruitmentApplicant1 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user1, role1);
        RecruitmentApplicant recruitmentApplicant2 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user2, role2);

        // when
        Map<Long, Long> recruitedCounts = recruitmentApplicantService.getRecruitedCounts(
                List.of(recruitmentApplicant1, recruitmentApplicant2));

        // then
        Assertions.assertThat(recruitedCounts.get(1L)).isEqualTo(1L);
        Assertions.assertThat(recruitedCounts.get(2L)).isEqualTo(1L);
    }

    @Test
    void 구인된역할수조회_예외발생_role이null일경우() {
        // given
        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상제목");
        User user1 = UserFixture.createUser();
        User user2 = UserFixture.createUser();
        RecruitmentApplicant recruitmentApplicant1 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user1, null);
        RecruitmentApplicant recruitmentApplicant2 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user2, null);

        // when
        Assertions.assertThatThrownBy(
                        () -> recruitmentApplicantService.getRecruitedCounts(
                                List.of(recruitmentApplicant1, recruitmentApplicant2)))
                .isInstanceOf(NullPointerException.class);

    }

    @Test
    void 신청승인_성공() {
        // given
        User user1 = UserFixture.createUser();
        Long userId1 = 1L;
        Role role1 = RoleFixture.createRole("백엔드개발자");
        role1.setId(1L);

        User user2 = UserFixture.createUser();
        Long userId2 = 1L;
        Role role2 = RoleFixture.createRole("프론트엔드개발자");
        role2.setId(2L);

        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상제목");
        recruitmentPost.setCreatedBy(userId1);

        RecruitmentApplicant applicant1 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user1, role1);
        RecruitmentApplicant applicant2 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user2, role2);

        doReturn(2L).when(recruitmentApplicantRepository).updateRecruitStatus(any(), any());

        // when, then
        recruitmentApplicantService.approveApplicants(List.of(applicant1, applicant2), List.of(userId1, userId2),
                userId1);
    }

    @Test
    void 신청승인_예외발생_작성자가아닌경우() {
        // given
        User user1 = UserFixture.createUser();
        Long userId1 = 1L;
        Role role1 = RoleFixture.createRole("백엔드개발자");
        role1.setId(1L);

        User user2 = UserFixture.createUser();
        Long userId2 = 1L;
        Role role2 = RoleFixture.createRole("프론트엔드개발자");
        role2.setId(2L);

        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상제목");
        recruitmentPost.setCreatedBy(0L);

        RecruitmentApplicant applicant1 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user1, role1);
        RecruitmentApplicant applicant2 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user2, role2);

        // when, then
        Assertions.assertThatThrownBy(
                        () -> recruitmentApplicantService.approveApplicants(List.of(applicant1, applicant2),
                                List.of(userId1, userId2), userId1))
                .isInstanceOf(RecruitmentApplicantException.class)
                .hasMessageContaining(INVALID_USER.message());
    }

    @Test
    void 신청승인_예외발생_신청상태가NONE아닌경우() {
        // given
        User user1 = UserFixture.createUser();
        Long userId1 = 1L;
        Role role1 = RoleFixture.createRole("백엔드개발자");
        role1.setId(1L);

        User user2 = UserFixture.createUser();
        Long userId2 = 1L;
        Role role2 = RoleFixture.createRole("프론트엔드개발자");
        role2.setId(2L);

        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상제목");
        recruitmentPost.setCreatedBy(userId1);

        RecruitmentApplicant applicant1 = RecruitmentApplicantFixture.createApprovedRecruitmentApplicant(
                recruitmentPost, user1, role1);
        RecruitmentApplicant applicant2 = RecruitmentApplicantFixture.createApprovedRecruitmentApplicant(
                recruitmentPost, user2, role2);

        // when, then
        Assertions.assertThatThrownBy(
                        () -> recruitmentApplicantService.approveApplicants(List.of(applicant1, applicant2),
                                List.of(userId1, userId2), userId1))
                .isInstanceOf(RecruitmentApplicantException.class)
                .hasMessageContaining(ALREADY_PROCESSED_APPLICANT.message());
    }

    @Test
    void 신청승인_예외발생_요청신청자와실제신청자가다를경우() {
        // given
        User user1 = UserFixture.createUser();
        Long userId1 = 1L;
        Role role1 = RoleFixture.createRole("백엔드개발자");
        role1.setId(1L);

        User user2 = UserFixture.createUser();
        Long userId2 = 1L;
        Role role2 = RoleFixture.createRole("프론트엔드개발자");
        role2.setId(2L);

        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상제목");
        recruitmentPost.setCreatedBy(userId1);

        RecruitmentApplicant applicant1 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user1, role1);

        // when, then
        Assertions.assertThatThrownBy(
                        () -> recruitmentApplicantService.approveApplicants(List.of(applicant1),
                                List.of(userId1, userId2), userId1))
                .isInstanceOf(RecruitmentApplicantException.class)
                .hasMessageContaining(INVALID_REQUEST.message());
    }

    @Test
    void 신청자거절_성공() {
        // given
        User user1 = UserFixture.createUser();
        Long userId1 = 1L;
        Role role1 = RoleFixture.createRole("백엔드개발자");
        role1.setId(1L);

        User user2 = UserFixture.createUser();
        Long userId2 = 1L;
        Role role2 = RoleFixture.createRole("프론트엔드개발자");
        role2.setId(2L);

        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상제목");
        recruitmentPost.setCreatedBy(userId1);

        RecruitmentApplicant applicant1 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user1, role1);
        applicant1.setId(1L);
        RecruitmentApplicant applicant2 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user2, role2);
        applicant2.setId(2L);

        doReturn(2L).when(recruitmentApplicantRepository).updateRecruitStatus(any(), any());
        doReturn(List.of(applicant1, applicant2)).when(recruitmentApplicantRepository).findAllInApplicantId(any());

        // when, then
        recruitmentApplicantService.rejectApplicants(List.of(userId1, userId2),
                userId1);
    }


    @Test
    void 신청자거절_예외발생_작성자가아닌경우() {
        // given
        User user1 = UserFixture.createUser();
        Long userId1 = 1L;
        Role role1 = RoleFixture.createRole("백엔드개발자");
        role1.setId(1L);

        User user2 = UserFixture.createUser();
        Long userId2 = 1L;
        Role role2 = RoleFixture.createRole("프론트엔드개발자");
        role2.setId(2L);

        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상제목");
        recruitmentPost.setCreatedBy(0L);

        RecruitmentApplicant applicant1 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user1, role1);
        applicant1.setId(1L);
        RecruitmentApplicant applicant2 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user2, role2);
        applicant2.setId(2L);

        doReturn(List.of(applicant1, applicant2)).when(recruitmentApplicantRepository).findAllInApplicantId(any());

        // when, then
        Assertions.assertThatThrownBy(
                        () -> recruitmentApplicantService.rejectApplicants(List.of(userId1, userId2), userId1))
                .isInstanceOf(RecruitmentApplicantException.class)
                .hasMessageContaining(INVALID_USER.message());
    }

    @Test
    void 신청자거절_예외발생_신청상태가NONE아닌경우() {
        // given
        User user1 = UserFixture.createUser();
        Long userId1 = 1L;
        Role role1 = RoleFixture.createRole("백엔드개발자");
        role1.setId(1L);

        User user2 = UserFixture.createUser();
        Long userId2 = 1L;
        Role role2 = RoleFixture.createRole("프론트엔드개발자");
        role2.setId(2L);

        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상제목");
        recruitmentPost.setCreatedBy(userId1);

        RecruitmentApplicant applicant1 = RecruitmentApplicantFixture.createApprovedRecruitmentApplicant(
                recruitmentPost, user1, role1);
        applicant1.setId(1L);

        RecruitmentApplicant applicant2 = RecruitmentApplicantFixture.createApprovedRecruitmentApplicant(
                recruitmentPost, user2, role2);
        applicant2.setId(2L);

        doReturn(List.of(applicant1, applicant2)).when(recruitmentApplicantRepository).findAllInApplicantId(any());

        // when, then
        Assertions.assertThatThrownBy(
                        () -> recruitmentApplicantService.rejectApplicants(List.of(userId1, userId2), userId1))
                .isInstanceOf(RecruitmentApplicantException.class)
                .hasMessageContaining(ALREADY_PROCESSED_APPLICANT.message());
    }

    @Test
    void 신청자거절_예외발생_요청신청자와실제신청자가다를경우() {
        // given
        User user1 = UserFixture.createUser();
        Long userId1 = 1L;
        Role role1 = RoleFixture.createRole("백엔드개발자");
        role1.setId(1L);

        User user2 = UserFixture.createUser();
        Long userId2 = 1L;
        Role role2 = RoleFixture.createRole("프론트엔드개발자");
        role2.setId(2L);

        RecruitmentPost recruitmentPost = RecruitmentPostFixture.createRecruitmentPost("정상제목");
        recruitmentPost.setCreatedBy(userId1);

        RecruitmentApplicant applicant1 = RecruitmentApplicantFixture.createRecruitmentApplicant(
                recruitmentPost, user1, role1);
        applicant1.setId(1L);

        doReturn(List.of(applicant1)).when(recruitmentApplicantRepository).findAllInApplicantId(any());

        // when, then
        Assertions.assertThatThrownBy(
                        () -> recruitmentApplicantService.rejectApplicants(List.of(userId1, userId2), userId1))
                .isInstanceOf(RecruitmentApplicantException.class)
                .hasMessageContaining(INVALID_REQUEST.message());
    }

    @Test
    void 신청자목록조회_NONE신청자목록조회_role설정하지않은경우() {
        // given
        Long postId = 1L;
        Long roleId = null;

        GetApplicantDto dto1 = new GetApplicantDto(1L, "1", "닉네임입니다1",
                "이미지입니다1", "이름입니다1", 4.3, "광운대학교", "소프트웨어학부", "qwer123@naver.com", 2018,
                "백엔드개발자", "전하는 말입니다1", 1L);
        GetApplicantDto dto2 = new GetApplicantDto(2L, "2", "닉네임입니다2",
                "이미지입니다2", "이름입니다2", 4.2, "광운대학교", "소프트웨어학부", "qwer456@naver.com", 2018,
                "백엔드개발자", "전하는 말입니다2", 1L);

        doReturn(new SliceImpl<>(
                List.of(dto1, dto2),
                PageRequest.of(1, 12),
                false
        )).when(recruitmentApplicantRepository).findByPostIdAndRoleId(any(), any(), any());

        doReturn("이미지입니다").when(cloudFrontService).getSignedUrl(any(), any(), any());

        try (MockedStatic<Encryption> utilities = Mockito.mockStatic(Encryption.class)) {
            utilities.when(() -> Encryption.encryptLong(any())).thenReturn("1234");

            // when
            GetApplicantResponseDto responseDtos = recruitmentApplicantService.getAllByRole(postId, roleId, 1L, 1L, 1,
                    12);

            // then
            Assertions.assertThat(responseDtos.applicants().size()).isEqualTo(2);

            Assertions.assertThat(responseDtos.applicants().get(0))
                    .extracting("nickname", "name", "applyRoleName")
                    .containsExactly("닉네임입니다1", "이름입니다1", "백엔드개발자");
            Assertions.assertThat(responseDtos.applicants().get(1))
                    .extracting("nickname", "name", "applyRoleName")
                    .containsExactly("닉네임입니다2", "이름입니다2", "백엔드개발자");
        }
    }
}
