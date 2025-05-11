package by.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class AddPhoneDto {
    @NotBlank
    @Size(max = 13)
    String phone;
}
