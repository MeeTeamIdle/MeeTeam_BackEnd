package synk.meeteam.domain.recruitment.recruitment_applicant.service;

import static synk.meeteam.domain.recruitment.recruitment_applicant.exception.RecruitmentApplicantExceptionType.INVALID_REQUEST;
import static synk.meeteam.domain.recruitment.recruitment_applicant.exception.RecruitmentApplicantExceptionType.INVALID_USER;
import static synk.meeteam.domain.recruitment.recruitment_applicant.exception.RecruitmentApplicantExceptionType.SS_602;
import static synk.meeteam.infra.aws.S3FilePath.USER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import synk.meeteam.domain.recruitment.recruitment_applicant.dto.response.GetApplicantDto;
import synk.meeteam.domain.recruitment.recruitment_applicant.dto.response.GetApplicantResponseDto;
import synk.meeteam.domain.recruitment.recruitment_applicant.entity.RecruitStatus;
import synk.meeteam.domain.recruitment.recruitment_applicant.entity.RecruitmentApplicant;
import synk.meeteam.domain.recruitment.recruitment_applicant.exception.RecruitmentApplicantException;
import synk.meeteam.domain.recruitment.recruitment_applicant.repository.RecruitmentApplicantRepository;
import synk.meeteam.domain.recruitment.recruitment_applicant.service.vo.RecruitmentApplicants;
import synk.meeteam.domain.recruitment.recruitment_post.entity.RecruitmentPost;
import synk.meeteam.domain.user.user.entity.User;
import synk.meeteam.global.dto.SliceInfo;
import synk.meeteam.global.entity.DeleteStatus;
import synk.meeteam.global.util.Encryption;
import synk.meeteam.infra.aws.service.CloudFrontService;

@Service
@RequiredArgsConstructor
public class RecruitmentApplicantService {
    private final RecruitmentApplicantRepository recruitmentApplicantRepository;
    private final CloudFrontService cloudFrontService;

    @Transactional
    public void registerRecruitmentApplicant(RecruitmentApplicant recruitmentApplicant) {
        // 이미 신청한 경우 검증로직
        validateAlreadyApply(recruitmentApplicant);

        recruitmentApplicantRepository.save(recruitmentApplicant);
    }

    @Transactional
    public void cancelRegisterRecruitmentApplicant(RecruitmentApplicant recruitmentApplicant) {
        recruitmentApplicant.softDelete();
    }

    @Transactional(readOnly = true)
    public List<RecruitmentApplicant> getAllApplicants(List<Long> applicantIds) {
        return recruitmentApplicantRepository.findAllInApplicantId(applicantIds);
    }

    @Transactional(readOnly = true)
    public RecruitmentApplicant getApplicant(RecruitmentPost recruitmentPost, User user) {
        return recruitmentApplicantRepository.findByRecruitmentPostAndApplicantOrElseThrow(recruitmentPost, user);
    }

    @Transactional(readOnly = true)
    public List<Long> getRoleIds(List<RecruitmentApplicant> approvedApplicants) {
        return approvedApplicants.stream()
                .map(applicant -> applicant.getRole().getId()).toList();
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getRecruitedCounts(List<RecruitmentApplicant> approvedApplicants) {
        Map<Long, Long> recruitedRoleCounts = new HashMap<>();
        approvedApplicants.stream()
                .forEach(applicant -> {
                    recruitedRoleCounts.putIfAbsent(applicant.getRole().getId(), 0L);

                    recruitedRoleCounts.put(applicant.getRole().getId(),
                            recruitedRoleCounts.get(applicant.getRole().getId()) + 1);
                });

        return recruitedRoleCounts;
    }

    @Transactional
    public void approveApplicants(List<RecruitmentApplicant> applicants, List<Long> applicantIds, Long userId) {
        // TODO : List<RecruitmentApplicant> 일급컬렉션으로 리팩토링 필요.
        validateCanProcess(applicants, userId);
        validateApplicants(applicantIds.size(), applicants.size());

        recruitmentApplicantRepository.updateRecruitStatus(applicantIds, RecruitStatus.APPROVED);
    }

    @Transactional
    public void rejectApplicants(List<Long> requestApplicantIds, Long writerId) {
        List<RecruitmentApplicant> applicants = getAllApplicants(requestApplicantIds);
        RecruitmentApplicants recruitmentApplicants = new RecruitmentApplicants(applicants, requestApplicantIds,
                writerId);

        recruitmentApplicantRepository.updateRecruitStatus(recruitmentApplicants.getRecruitmentApplicantIds(),
                RecruitStatus.REJECTED);
    }

    @Transactional
    public GetApplicantResponseDto getAllByRole(Long postId, Long roleId, Long userId, Long writerId, int page,
                                                int size) {
        validateIsWriter(userId, writerId);

        int pageNumber = page - 1;
        Pageable pageable = PageRequest.of(pageNumber, size);

        Slice<GetApplicantDto> applicantDtos = recruitmentApplicantRepository.findByPostIdAndRoleId(postId,
                roleId, pageable);
        applicantDtos.stream().forEach(applicant -> applicant.setEncryptedUserIdAndProfileImg(
                Encryption.encryptLong(Long.parseLong(applicant.getUserId())),
                cloudFrontService.getSignedUrl(USER, applicant.getProfileImg(), applicant.getVersion())));

        SliceInfo pageInfo = new SliceInfo(page, size, applicantDtos.hasNext());
        return new GetApplicantResponseDto(applicantDtos.getContent(), pageInfo);
    }

    @Transactional(readOnly = true)
    public boolean isAppliedUser(RecruitmentPost recruitmentPost, User user) {
        RecruitmentApplicant recruitmentApplicant = recruitmentApplicantRepository.findByRecruitmentPostAndApplicantAndDeleteStatus(
                recruitmentPost, user, DeleteStatus.ALIVE).orElse(null);

        if (recruitmentApplicant != null) {
            return false;
        }
        return true;
    }

    private void validateIsWriter(Long userId, Long writerId) {
        if (!userId.equals(writerId)) {
            throw new RecruitmentApplicantException(INVALID_USER);
        }
    }

    private void validateCanProcess(List<RecruitmentApplicant> applicants, Long userId) {
        // applicants 검증로직
        // 호출한 사용자가 구인글 작성자인지 확인
        // status가 다 NONE인지 확인

        applicants.stream()
                .forEach(applicant -> applicant.validateCanApprove(userId));
    }

    private void validateApplicants(int requestCount, int actualCount) {
        if (requestCount != actualCount) {
            throw new RecruitmentApplicantException(INVALID_REQUEST);
        }
    }

    private void validateAlreadyApply(RecruitmentApplicant recruitmentApplicant) {
        RecruitmentApplicant alreadyAppliedUser = recruitmentApplicantRepository.findByRecruitmentPostAndApplicantAndDeleteStatus(
                        recruitmentApplicant.getRecruitmentPost(), recruitmentApplicant.getApplicant(), DeleteStatus.ALIVE)
                .orElse(null);
        if (alreadyAppliedUser != null) {
            throw new RecruitmentApplicantException(SS_602);
        }
    }
}
