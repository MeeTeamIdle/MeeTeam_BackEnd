package synk.meeteam.domain.user.user.entity;

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.crypto.password.PasswordEncoder;
import synk.meeteam.domain.common.department.entity.Department;
import synk.meeteam.domain.common.role.entity.Role;
import synk.meeteam.domain.common.university.entity.University;
import synk.meeteam.domain.user.user.entity.enums.Authority;
import synk.meeteam.domain.user.user.entity.enums.PlatformType;
import synk.meeteam.global.entity.BaseTimeEntity;
import synk.meeteam.global.util.Encryption;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "USERS")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    //이메일
    @NotNull
    @Size(max = 100)
    @Column(length = 100, updatable = false)
    private String email;

    //이름
    @NotNull
    @Size(max = 20)
    @Column(length = 20)
    private String name;

    //닉네임
    @NotNull
    @NotBlank
    @Size(min = 4, max = 16)
    @Column(length = 16, unique = true)
    private String nickname;

    //비밀번호
    @Column(length = 16)
    private String password;

    @ColumnDefault("1")
    private boolean isEmailPublic = true;

    //전화번호
    @Column(length = 15)
    private String phoneNumber;

    @ColumnDefault("0")
    private boolean isPhonePublic = false;

    //한줄 소개
    @Column(length = 20)
    private String oneLineIntroduction;

    //자기 소개
    @Column(columnDefinition = "TEXT")
    private String mainIntroduction;

    //학점
    private double gpa = 0.0;

    //최대학점
    private double maxGpa = 4.5;

    //입학년도
    @NotNull
    private Integer admissionYear;

    //프로필 이미지 url
    @Column(length = 300)
    private String pictureUrl;

    //평가 점수
    @ColumnDefault("0")
    private double evaluationScore;

    //학교
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "university_id", updatable = false)
    private University university;

    //학과
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "department_id")
    private Department department;

    //관심있는 역할
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "interest_role_id")
    private Role interestRole;

    @Enumerated(EnumType.STRING)
    private Authority authority;

    @Enumerated(EnumType.STRING)
    private PlatformType platformType; // KAKAO, NAVER, GOOGLE, NONE

    private String platformId;

    //평가 점수

    @ColumnDefault("0")
    private long scoreTime = 0;


    @ColumnDefault("0")
    private long scoreInfluence = 0;


    @ColumnDefault("0")
    private long scoreParticipation = 0;


    @ColumnDefault("0")
    private long scoreCommunication = 0;


    @ColumnDefault("0")
    private long scoreProfessionalism = 0;

    @Builder
    public User(String email, String name, String nickname, String password, String phoneNumber,
                Integer admissionYear, String pictureUrl, Authority authority, PlatformType platformType,
                String platformId,
                University university, Department department) {
        this.email = email;
        this.university = university;
        this.department = department;
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.nickname = nickname;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.admissionYear = admissionYear;
        this.authority = authority;
        this.platformType = platformType;
        this.platformId = platformId;
    }

    public String encryptUserId() {
        return Encryption.encryptLong(this.id);
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateAdmissionYear(int admissionYear) {
        this.admissionYear = admissionYear;
    }

    public Long decryptUserId(String encryptedUserId) {
        return Encryption.decryptLong(encryptedUserId);
    }

    public void updateUniversity(University university) {
        this.university = university;
    }

    public void updateAuthority(Authority authority) {
        this.authority = authority;
    }

    // 비밀번호 암호화 메소드
    public void passwordEncode(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    // 유저 권한 설정 메소드
    public void authorizeUser() {
        this.authority = Authority.USER;
    }
}
