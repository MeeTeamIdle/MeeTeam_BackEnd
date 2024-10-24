package synk.meeteam.domain.recruitment.recruitment_post.repository.vo;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import synk.meeteam.global.entity.Category;
import synk.meeteam.global.entity.Scope;

@Data
public class RecruitmentPostVo {
    private Long id;
    private String title;
    private Category category;
    private Scope scope;
    private Long writerId;
    private String writerNickname;
    private String writerProfileImg;
    private LocalDate deadline;
    private Boolean isBookmarked;
    private LocalDateTime createdAt;
    private Boolean isClosed;
    private Long writerVersion;

    @Builder
    @QueryProjection
    public RecruitmentPostVo(Long id, String title, Category category, Scope scope, Long writerId,
                             String writerNickname,
                             String writerProfileImg, LocalDate deadline, Boolean isBookmarked, LocalDateTime createdAt,
                             Boolean isClosed,
                             Long writerVersion) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.scope = scope;
        this.writerId = writerId;
        this.writerNickname = writerNickname;
        this.writerProfileImg = writerProfileImg;
        this.deadline = deadline;
        this.isBookmarked = isBookmarked;
        this.createdAt = createdAt;
        this.isClosed = isClosed;
        this.writerVersion = writerVersion;
    }
}
