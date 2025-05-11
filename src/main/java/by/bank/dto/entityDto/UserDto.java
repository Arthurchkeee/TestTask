package by.bank.dto.entityDto;

import by.bank.entity.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for {@link User}
 */
@Value
public class UserDto implements Serializable {
    @NotNull
    @Positive
    Long id;
    @NotNull
    @Size(max = 500)
    String name;
    @NotNull
    @Past
    LocalDate dateOfBirth;
    @NotNull
    List<EmailDataDto> emails;
    @NotNull
    AccountDto account;
    @NotNull
    List<PhoneDataDto> phones;
}