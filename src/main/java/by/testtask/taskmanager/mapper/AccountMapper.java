package by.testtask.taskmanager.mapper;

import by.testtask.taskmanager.dto.entityDto.AccountDto;
import by.testtask.taskmanager.entity.Account;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    Account toEntity(AccountDto accountDto);

    AccountDto toDto(Account account);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Account partialUpdate(AccountDto accountDto, @MappingTarget Account account);

    List<Account> toEntity(List<AccountDto> accountDto);

    List<AccountDto> toDto(List<Account> account);
}