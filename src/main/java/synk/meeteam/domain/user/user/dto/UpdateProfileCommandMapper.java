package synk.meeteam.domain.user.user.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import synk.meeteam.domain.user.user.dto.command.UpdateProfileCommand;
import synk.meeteam.domain.user.user.dto.request.UpdateProfileRequestDto;

@Mapper(componentModel = "spring")
public interface UpdateProfileCommandMapper {

    @Mapping(source = "imageUrl", target = "pictureUrl")
    @Mapping(source = "introduction", target = "oneLineIntroduction")
    @Mapping(source = "aboutMe", target = "mainIntroduction")
    UpdateProfileCommand toUpdateProfileCommand(UpdateProfileRequestDto updateProfileRequestDto);
}
