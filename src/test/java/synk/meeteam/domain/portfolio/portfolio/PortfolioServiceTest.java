package synk.meeteam.domain.portfolio.portfolio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static synk.meeteam.domain.portfolio.portfolio.exception.PortfolioExceptionType.NOT_FOUND_PORTFOLIO;
import static synk.meeteam.domain.portfolio.portfolio.exception.PortfolioExceptionType.OVER_MAX_PIN_SIZE;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import synk.meeteam.domain.common.field.entity.Field;
import synk.meeteam.domain.common.field.repository.FieldRepository;
import synk.meeteam.domain.common.role.RoleFixture;
import synk.meeteam.domain.common.role.repository.RoleRepository;
import synk.meeteam.domain.portfolio.portfolio.dto.command.CreatePortfolioCommand;
import synk.meeteam.domain.portfolio.portfolio.dto.response.GetUserPortfolioResponseDto;
import synk.meeteam.domain.portfolio.portfolio.entity.Portfolio;
import synk.meeteam.domain.portfolio.portfolio.exception.PortfolioException;
import synk.meeteam.domain.portfolio.portfolio.repository.PortfolioRepository;
import synk.meeteam.domain.portfolio.portfolio.service.PortfolioService;
import synk.meeteam.domain.portfolio.portfolio.service.PortfolioServiceImpl;
import synk.meeteam.domain.user.user.entity.User;
import synk.meeteam.global.entity.ProceedType;
import synk.meeteam.infra.aws.service.CloudFrontService;

@ExtendWith(MockitoExtension.class)
public class PortfolioServiceTest {
    @Mock
    private PortfolioService portfolioService;

    @InjectMocks
    private PortfolioServiceImpl portfolioServiceImpl;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CloudFrontService cloudFrontService;

    @BeforeEach
    void setup() {

        portfolioService = portfolioServiceImpl;
    }

    @Test
    void 포트폴리오핀설정_핀설정성공_정상핀아이디목록() {
        //given
        doReturn(PortfolioFixture.createPortfolioFixtures_1_2()).when(portfolioRepository)
                .findAllByCreatedByAndIsPinTrue(anyLong());
        List<Long> pins = List.of(2L, 1L);
        doReturn(PortfolioFixture.createPortfolioFixtures_2_1()).when(portfolioRepository)
                .findAllByCreatedByAndIsPinTrueOrderByIds(anyLong(), eq(pins));
        //when
        List<Portfolio> portfolios = portfolioService.changePinPortfoliosByIds(1L, pins);
        //then
        assertThat(portfolios).extracting("isPin").containsExactly(true, true);
        assertThat(portfolios).extracting("pinOrder").containsExactly(1, 2);
        assertThat(portfolios).extracting("title").containsExactly("타이틀2", "타이틀1");
    }

    @Test
    void 포트폴리오핀설정_핀설정실패_조회되지않는아이디() {
        //given
        List<Long> pins = List.of(2L, 1L, 3L);

        //유저가 소유주가 아닌 경우 or
        doReturn(PortfolioFixture.createPortfolioFixtures_2_1()).when(portfolioRepository)
                .findAllByCreatedByAndIsPinTrueOrderByIds(anyLong(), eq(pins));
        //when then
        assertThatThrownBy(() -> portfolioService.changePinPortfoliosByIds(1L, pins))
                .isExactlyInstanceOf(PortfolioException.class)
                .hasMessage(NOT_FOUND_PORTFOLIO.message());
    }

    @Test
    void 포트폴리오핀설정_핀설정실패_8개를넘는포트폴리오() {
        //given
        List<Long> pins = List.of(2L, 1L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);
        //when then
        assertThatThrownBy(() -> portfolioService.changePinPortfoliosByIds(1L, pins))
                .isExactlyInstanceOf(PortfolioException.class)
                .hasMessage(OVER_MAX_PIN_SIZE.message());
    }

    @Test
    void 포트폴리오목록조회_목록조회성공() {
        //given
        doReturn(PortfolioFixture.createPortfolioFixtures_1_2()).when(portfolioRepository)
                .findAllByIsPinTrueAndCreatedByOrderByPinOrderAsc(anyLong());
        //when
        List<Portfolio> userPortfolio = portfolioService.getMyPinPortfolio(anyLong());
        //then
        assertThat(userPortfolio).extracting("title").containsExactly("타이틀1", "타이틀2");
    }

    @Test
    void 내포트폴리오목록조회_목록조회성공() {
        //given
        doReturn(PortfolioFixture.createSlicePortfolioDtos()).when(portfolioRepository)
                .findSlicePortfoliosByUserOrderByCreatedAtDesc(eq(PageRequest.of(0, 12)), any());
        doReturn("url입니다.").when(cloudFrontService).getSignedUrl(any(), any(), any());
        //when
        GetUserPortfolioResponseDto userAllPortfolios = portfolioService.getSliceMyAllPortfolio(1, 12,
                User.builder().build());

        //then
        assertThat(userAllPortfolios.portfolios()).extracting("title").containsExactly("타이틀1", "타이틀2");
        assertThat(userAllPortfolios.pageInfo().page()).isEqualTo(1);
        assertThat(userAllPortfolios.pageInfo().size()).isEqualTo(12);
        assertThat(userAllPortfolios.pageInfo().hasNextPage()).isEqualTo(false);
    }

    @Test
    void 포트폴리오등록_등록성공() {
        //given
        doReturn(RoleFixture.createRole("개발자")).when(roleRepository).findByIdOrElseThrowException(anyLong());
        doReturn(new Field(1L, "개발")).when(fieldRepository).findByIdOrElseThrowException(anyLong());
        doReturn(PortfolioFixture.createPortfolioFixture()).when(portfolioRepository).saveAndFlush(any());
        //when
        Portfolio portfolio = portfolioService.postPortfolio(new CreatePortfolioCommand(
                "제목",
                "부연설명",
                "컨텐츠",
                1L, 1L, LocalDate.now(), LocalDate.now(), ProceedType.ON_LINE, "이미지1.png", "이미지.zip",
                List.of("이미지1.png", "이미지2.png")
        ));
        //then
        assertThat(portfolio.getId()).isNotNull();
        assertThat(portfolio.getTitle()).isEqualTo("제목");
        assertThat(portfolio.getDescription()).isEqualTo("부연설명");
    }


}
