package by.bank.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.math.BigDecimal;
@Value
public class TransferDto {
    @NotNull
    Long transferTo;
    @NotNull
    @Positive
    BigDecimal value;
}
