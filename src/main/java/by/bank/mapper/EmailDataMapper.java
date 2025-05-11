package by.bank.mapper;

import by.bank.dto.entityDto.EmailDataDto;
import by.bank.entity.EmailData;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmailDataMapper {
    EmailData toEntity(EmailDataDto emailDataDto);

    EmailDataDto toDto(EmailData emailData);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    EmailData partialUpdate(EmailDataDto emailDataDto, @MappingTarget EmailData emailData);

    List<EmailData> toEntity(List<EmailDataDto> emailDataDto);

    List<EmailDataDto> toDto(List<EmailData> emailData);
}