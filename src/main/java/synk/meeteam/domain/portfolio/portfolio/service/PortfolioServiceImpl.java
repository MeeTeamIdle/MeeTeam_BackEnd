package synk.meeteam.domain.portfolio.portfolio.service;

import static synk.meeteam.domain.portfolio.portfolio.exception.PortfolioExceptionType.NOT_FOUND_PORTFOLIO;
import static synk.meeteam.domain.portfolio.portfolio.exception.PortfolioExceptionType.OVER_MAX_PIN_SIZE;
import static synk.meeteam.domain.portfolio.portfolio.exception.PortfolioExceptionType.SS_110;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import synk.meeteam.domain.common.field.entity.Field;
import synk.meeteam.domain.common.field.repository.FieldRepository;
import synk.meeteam.domain.common.role.entity.Role;
import synk.meeteam.domain.common.role.repository.RoleRepository;
import synk.meeteam.domain.portfolio.portfolio.dto.GetProfilePortfolioDto;
import synk.meeteam.domain.portfolio.portfolio.dto.command.CreatePortfolioCommand;
import synk.meeteam.domain.portfolio.portfolio.dto.command.UpdatePortfolioCommand;
import synk.meeteam.domain.portfolio.portfolio.dto.response.GetUserPortfolioResponseDto;
import synk.meeteam.domain.portfolio.portfolio.entity.Portfolio;
import synk.meeteam.domain.portfolio.portfolio.exception.PortfolioException;
import synk.meeteam.domain.portfolio.portfolio.repository.PortfolioRepository;
import synk.meeteam.domain.user.user.entity.User;
import synk.meeteam.global.dto.SliceInfo;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final FieldRepository fieldRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public List<Portfolio> changePinPortfoliosByIds(Long userId, List<Long> portfolioIds) {

        if (portfolioIds.size() > Portfolio.MAX_PIN_SIZE) {
            throw new PortfolioException(OVER_MAX_PIN_SIZE);
        }

        //포트폴리오 조회
        List<Portfolio> newPinPortfolios = portfolioRepository.findAllByCreatedByAndIsPinTrueOrderByIds(userId,
                portfolioIds);

        //조회한 결과와 요청한 id의 갯수가 상이할 경우
        if (newPinPortfolios.size() != portfolioIds.size()) {
            throw new PortfolioException(NOT_FOUND_PORTFOLIO);
        }

        //기존 핀 해제
        List<Portfolio> oldPinPortfolios = portfolioRepository.findAllByCreatedByAndIsPinTrue(userId);
        oldPinPortfolios.forEach(Portfolio::unpin);

        //핀 설정
        for (int index = 0; index < newPinPortfolios.size(); index++) {
            newPinPortfolios.get(index).putPin(index + 1);
        }

        return newPinPortfolios;
    }

    @Override
    public List<Portfolio> getMyPinPortfolio(Long userId) {
        return portfolioRepository.findAllByIsPinTrueAndCreatedByOrderByPinOrderAsc(userId);
    }

    @Override
    public GetUserPortfolioResponseDto getMyAllPortfolio(int page, int size, User user) {
        int pageNumber = page - 1;
        Pageable pageable = PageRequest.of(pageNumber, size);
        Slice<GetProfilePortfolioDto> userPortfolioDtos = portfolioRepository.findUserPortfoliosByUserOrderByCreatedAtDesc(
                pageable, user);
        SliceInfo pageInfo = new SliceInfo(page, size, userPortfolioDtos.hasNext());
        return new GetUserPortfolioResponseDto(userPortfolioDtos.getContent(), pageInfo);
    }

    @Override
    @Transactional
    public Portfolio postPortfolio(CreatePortfolioCommand command) {
        Field field = fieldRepository.findByIdOrElseThrowException(command.fieldId());
        Role role = roleRepository.findByIdOrElseThrowException(command.roleId());
        Portfolio portfolio = Portfolio.builder()
                .title(command.title())
                .description(command.description())
                .content(command.content())
                .proceedStart(command.proceedStart())
                .proceedEnd(command.proceedEnd())
                .proceedType(command.proceedType())
                .mainImageFileName(command.mainImageFileName())
                .zipFileName(command.zipFileName())
                .fileOrder(command.fileOrder())
                .field(field)
                .role(role)
                .isPin(false)
                .pinOrder(0)
                .build();

        return portfolioRepository.saveAndFlush(portfolio);
    }

    @Override
    public Portfolio editPortfolio(Portfolio portfolio, User user, UpdatePortfolioCommand command) {
        Field field = fieldRepository.findByIdOrElseThrowException(command.fieldId());
        Role role = roleRepository.findByIdOrElseThrowException(command.roleId());
        portfolio.updatePortfolio(
                command.title(),
                command.description(),
                command.content(),
                command.proceedStart(),
                command.proceedEnd(),
                command.proceedType(),
                field,
                role,
                command.fileOrder()
        );
        return portfolio;
    }

    @Override
    public Portfolio getPortfolio(Long portfolioId) {
        return portfolioRepository.findByIdWithFieldAndRoleOrElseThrow(portfolioId);
    }

    public Portfolio getPortfolio(Long portfolioId, User user) {
        Portfolio portfolio = portfolioRepository.findByIdOrElseThrow(portfolioId);

        if (!portfolio.getCreatedBy().equals(user.getId())) {
            throw new PortfolioException(SS_110);
        }

        return portfolio;
    }
}
