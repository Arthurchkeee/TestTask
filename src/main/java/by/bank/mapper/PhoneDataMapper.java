package by.bank.mapper;

import by.bank.dto.entityDto.PhoneDataDto;
import by.bank.entity.PhoneData;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PhoneDataMapper {
    PhoneData toEntity(PhoneDataDto phoneDataDto);

    PhoneDataDto toDto(PhoneData phoneData);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PhoneData partialUpdate(PhoneDataDto phoneDataDto, @MappingTarget PhoneData phoneData);

    List<PhoneData> toEntity(List<PhoneDataDto> phoneDataDto);

    List<PhoneDataDto> toDto(List<PhoneData> phoneData);
}