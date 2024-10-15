package synk.meeteam.domain.portfolio.portfolio.entity;

import static jakarta.persistence.FetchType.LAZY;
import static synk.meeteam.domain.portfolio.portfolio.exception.PortfolioExceptionType.NOT_YOUR_PORTFOLIO;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import synk.meeteam.domain.common.field.entity.Field;
import synk.meeteam.domain.common.role.entity.Role;
import synk.meeteam.domain.portfolio.portfolio.exception.PortfolioException;
import synk.meeteam.global.entity.BaseEntity;
import synk.meeteam.global.entity.DeleteStatus;
import synk.meeteam.global.entity.ProceedType;
import synk.meeteam.global.util.StringListConverter;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.MODULE)
public class Portfolio extends BaseEntity {

    public static int MAX_PIN_SIZE = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long id;

    //제목
    @NotNull
    @Size(max = 40)
    @Column(length = 40)
    private String title;

    //부연설명
    @NotNull
    private String description;

    //내용
    @NotNull
    @Column(columnDefinition = "TEXT")
    private String content;

    //진행기간 시작일
    @NotNull
    private LocalDate proceedStart;

    //진행기간 종료일
    @NotNull
    private LocalDate proceedEnd;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ProceedType proceedType;

    @NotNull
    @Convert(converter = StringListConverter.class)
    private List<String> fileOrder;
    //분야
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "field_id")
    private Field field;

    //역할
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "role_id")
    private Role role;

    //포트폴리오 커버 이미지
    @NotNull
    private String mainImageFileName;

    @NotNull
    private String zipFileName;

    //핀인지 여부
    @ColumnDefault("0")
    private Boolean isPin;

    //핀 순서
    @ColumnDefault("0")
    private int pinOrder;

    //삭제 여부
    @NotNull
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ALIVE'")
    private DeleteStatus deleteStatus = DeleteStatus.ALIVE;

    @ColumnDefault("1")
    private long version = 1;

    @Builder
    public Portfolio(Long id, String title, String description, String content, LocalDate proceedStart,
                     LocalDate proceedEnd,
                     ProceedType proceedType, Field field, Role role, String mainImageFileName, String zipFileName,
                     Boolean isPin, int pinOrder,
                     List<String> fileOrder) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.proceedStart = proceedStart;
        this.proceedEnd = proceedEnd;
        this.proceedType = proceedType;
        this.field = field;
        this.role = role;
        this.mainImageFileName = mainImageFileName;
        this.zipFileName = zipFileName;
        this.fileOrder = fileOrder;
        this.isPin = isPin;
        this.pinOrder = pinOrder;
    }

    public void updatePortfolio(String title, String description, String content, LocalDate proceedStart,
                                LocalDate proceedEnd, ProceedType proceedType, Field field, Role role,
                                List<String> fileOrder, String mainImageFileName) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.proceedStart = proceedStart;
        this.proceedEnd = proceedEnd;
        this.proceedType = proceedType;
        this.field = field;
        this.role = role;
        this.fileOrder = fileOrder;
        this.mainImageFileName = mainImageFileName;
    }

    public boolean isAllViewAble(Long userId) {
        return isWriter(userId) || isPin;
    }

    public boolean isWriter(Long userId) {
        return getCreatedBy().equals(userId);
    }

    public void validWriter(Long userId) {
        if (!isWriter(userId)) {
            throw new PortfolioException(NOT_YOUR_PORTFOLIO);
        }
    }

    public void putPin(int order) {
        isPin = true;
        pinOrder = order;
    }

    public void unpin() {
        isPin = false;
        pinOrder = 0;
    }

    public void softDelete() {
        this.deleteStatus = DeleteStatus.DELETED;
    }
}
