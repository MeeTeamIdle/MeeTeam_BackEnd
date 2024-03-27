package synk.meeteam.domain.portfolio.portfolio.entity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import synk.meeteam.domain.common.field.entity.Field;
import synk.meeteam.domain.common.role.entity.Role;
import synk.meeteam.domain.portfolio.portfolio.dto.GetProfilePortfolioDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface PortfolioMapper {

    @Named("toField")
    static String toField(Field field) {
        return field.getName();
    }

    @Named("toRole")
    static String toRole(Role role) {
        return role.getName();
    }

    @Mapping(source = "portfolio.proceedStart", target = "startDate")
    @Mapping(source = "portfolio.proceedEnd", target = "endDate")
    @Mapping(target = "field", qualifiedByName = "toField")
    @Mapping(target = "role", qualifiedByName = "toRole")
    GetProfilePortfolioDto toGetProfilePortfolioDto(Portfolio portfolio, String mainImageUrl);
}
