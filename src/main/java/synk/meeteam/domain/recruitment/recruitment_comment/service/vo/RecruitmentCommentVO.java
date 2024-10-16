package synk.meeteam.domain.recruitment.recruitment_comment.service.vo;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RecruitmentCommentVO {
    Long id;
    Long userId;
    String nickname;
    String profileImg;
    String content;
    String createAt;
    boolean isParent;
    long groupNumber;
    long groupOrder;
    boolean isDeleted;
    Long userVersion;

    @Builder
    @QueryProjection
    public RecruitmentCommentVO(Long id, Long userId, String nickname, String profileImg, String content,
                                LocalDateTime createAt,
                                boolean isParent, long groupNumber, long groupOrder, boolean isDeleted,
                                Long userVersion) {
        this.id = id;
        this.userId = userId;
        this.nickname = nickname;
        this.profileImg = profileImg;
        this.content = content;
        this.createAt = createAt.toString();
        this.isParent = isParent;
        this.groupNumber = groupNumber;
        this.groupOrder = groupOrder;
        this.isDeleted = isDeleted;
        this.userVersion = userVersion;
    }

    public Long getUserId() {
        if (isDeleted) {
            return null;
        }
        return userId;
    }

    public String getProfileImg() {
        if (isDeleted) {
            return null;
        }
        return profileImg;
    }

    public String getNickname() {
        if (isDeleted) {
            return null;
        }
        return nickname;
    }

    public String getContent() {
        if (isDeleted) {
            return null;
        }
        return content;
    }
}
