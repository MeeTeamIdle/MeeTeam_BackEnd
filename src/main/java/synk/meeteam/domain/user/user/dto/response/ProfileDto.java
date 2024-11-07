package synk.meeteam.domain.user.user.dto.response;

public record ProfileDto(
        String profileImgFileName,
        String name,
        boolean isPublicName,
        String nickname,
        String interest,
        String oneLineIntroduction,
        String mainIntroduction,
        boolean isUniversityMainEmail,
        String universityEmail,
        boolean isPublicUniversityEmail,
        String subEmail,
        boolean isPublicSubEmail,
        String phoneNumber,
        boolean isPublicPhone,
        String university,
        String department,
        Double maxGpa,
        Double gpa,
        int admissionYear,
        long imgVersion
) {
}
