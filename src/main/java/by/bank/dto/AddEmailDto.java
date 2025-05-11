package by.bank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class AddEmailDto {
    @Email
    @Size(max = 200)
    @NotBlank
    String email;
}
