package synk.meeteam.domain.recruitment.recruitment_applicant.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(name = "GetApplicantDto", description = "신청자 조회 Dto")
@Getter
public class GetApplicantDto {

    @Schema(description = "신청 id", example = "1")
    Long applicantId;
    @Schema(description = "유저 id", example = "4OaVE421DSwR63xfKf6vxA==")
    String userId;
    @Schema(description = "신청자 닉네임", example = "minji_98")
    String nickname;
    @Schema(description = "신청자 프로필 사진", example = "url형태")
    String profileImg;
    @Schema(description = "신청자 이름", example = "김민지")
    String name;
    @Schema(description = "신청자 평점", example = "4.4")
    double score;
    @Schema(description = "대학 이름", example = "광운대학교")
    String universityName;
    @Schema(description = "학과 이름", example = "소프트웨어학부")
    String departmentName;
    @Schema(description = "이메일", example = "qwer123@naver.com")
    String email;
    @Schema(description = "입학년도", example = "2018")
    int year;
    @Schema(description = "신청 역할", example = "백엔드개발자")
    String applyRoleName;
    @Schema(description = "전하는 말", example = "저 관심있어용")
    String message;
    @Schema(description = "버전", example = "1")
    Long version;

    @QueryProjection
    public GetApplicantDto(Long applicantId, String userId, String nickname, String profileImg, String name,
                           double score,
                           String universityName, String departmentName, String email, int year, String applyRoleName,
                           String message, Long version) {
        this.applicantId = applicantId;
        this.userId = userId;
        this.nickname = nickname;
        this.profileImg = profileImg;
        this.name = name;
        this.score = score;
        this.universityName = universityName;
        this.departmentName = departmentName;
        this.email = email;
        this.year = year;
        this.applyRoleName = applyRoleName;
        this.message = message;
        this.version = version;
    }

    public void setEncryptedUserIdAndProfileImg(String userId, String profileImg) {
        this.userId = userId;
        this.profileImg = profileImg;
    }
}
