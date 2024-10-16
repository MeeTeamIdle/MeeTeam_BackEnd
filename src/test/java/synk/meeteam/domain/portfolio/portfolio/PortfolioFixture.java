package synk.meeteam.domain.portfolio.portfolio;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import synk.meeteam.domain.common.field.entity.Field;
import synk.meeteam.domain.common.role.entity.Role;
import synk.meeteam.domain.portfolio.portfolio.dto.SimplePortfolioDto;
import synk.meeteam.domain.portfolio.portfolio.dto.response.GetUserPortfolioResponseDto;
import synk.meeteam.domain.portfolio.portfolio.entity.Portfolio;
import synk.meeteam.global.dto.SliceInfo;
import synk.meeteam.global.entity.ProceedType;

public class PortfolioFixture {
    public static List<Portfolio> createPortfolioFixtures_1_2() {
        return List.of(
                Portfolio.builder().id(1L).title("타이틀1").description("디스크립션1").isPin(true).pinOrder(1).build(),
                Portfolio.builder().id(2L).title("타이틀2").description("디스크립션2").isPin(true).pinOrder(2).build()
        );
    }

    public static List<Portfolio> createPortfolioFixtures_2_1() {
        return List.of(
                Portfolio.builder().id(2L).title("타이틀2").description("디스크립션2").isPin(true).pinOrder(2).build(),
                Portfolio.builder().id(1L).title("타이틀1").description("디스크립션1").isPin(true).pinOrder(1).build()
        );
    }

    public static Portfolio createPortfolioFixture() {
        return Portfolio.builder()
                .id(1L)
                .title("제목")
                .content("컨텐츠")
                .description("부연설명")
                .proceedStart(LocalDate.of(24, 2, 1))
                .proceedEnd(LocalDate.now())
                .proceedType(ProceedType.ON_LINE)
                .isPin(false)
                .pinOrder(0)
                .mainImageFileName("이미지.png")
                .zipFileName("집.zip")
                .fileOrder(List.of("집1", "집2"))
                .role(new Role("개발자"))
                .field(new Field(1L, "개발"))
                .build();
    }

    public static Slice<SimplePortfolioDto> createSlicePortfolioDtos() {
        return new SliceImpl<>(
                List.of(
                        new SimplePortfolioDto(1L, "타이틀1", "이미지url", "개발", "개발자", true, 1, 1L),
                        new SimplePortfolioDto(2L, "타이틀2", "이미지url", "개발", "개발자", true, 2, 1L)
                ),
                PageRequest.of(1, 12),
                false
        );
    }

    public static GetUserPortfolioResponseDto createUserAllPortfolios() {
        return new GetUserPortfolioResponseDto(
                List.of(
                        new SimplePortfolioDto(1L, "타이틀1", "이미지url", "개발", "개발자", true, 1, 1L),
                        new SimplePortfolioDto(2L, "타이틀2", "이미지url", "개발", "개발자", true, 2, 1L)
                ),
                new SliceInfo(1, 12, false)
        );
    }
}
