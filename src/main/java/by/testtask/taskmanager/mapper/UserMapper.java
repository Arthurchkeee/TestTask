package by.testtask.taskmanager.mapper;

import by.testtask.taskmanager.dto.entityDto.UserDto;
import by.testtask.taskmanager.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toEntity(UserDto userDto);

    UserDto toDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User partialUpdate(UserDto userDto, @MappingTarget User user);

    List<User> toEntity(List<UserDto> userDto);

    List<UserDto> toDto(List<User> user);
}