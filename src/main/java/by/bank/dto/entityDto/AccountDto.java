package by.bank.dto.entityDto;

import by.bank.entity.Account;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link Account}
 */
@Value
public class AccountDto implements Serializable {
    @NotNull
    @Positive
    Long id;
    @NotNull
    @PositiveOrZero
    BigDecimal balance;
}