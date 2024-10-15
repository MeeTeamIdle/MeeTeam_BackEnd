package synk.meeteam.domain.portfolio.portfolio.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SimplePortfolioDto {
    @Schema(description = "포트폴리오 id", example = "1")
    private Long id;
    @Schema(description = "제목", example = "Meeteam")
    private String title;
    @Schema(description = "메인 커버이미지 url", example = "https://~~")
    private String mainImageUrl;
    @Schema(description = "분야", example = "개발")
    private String field;
    @Schema(description = "역할", example = "백엔드 개발자")
    private String role;
    @Schema(description = "핀여부", example = "true")
    private boolean isPinned;
    @Schema(description = "핀순서", example = "1")
    private int pinOrder;
    @Schema(description = "버전", example = "1")
    private Long version;

    @Builder
    @QueryProjection
    public SimplePortfolioDto(Long id, String title, String mainImageUrl, String field, String role,
                              boolean isPinned,
                              int pinOrder,
                              Long version) {
        this.id = id;
        this.title = title;
        this.mainImageUrl = mainImageUrl;
        this.field = field;
        this.role = role;
        this.isPinned = isPinned;
        this.pinOrder = pinOrder;
        this.version = version;
    }
}
