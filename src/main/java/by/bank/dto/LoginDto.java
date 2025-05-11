package by.bank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class LoginDto {
    @Email
    String email;
    String phone;
    @NotNull
    String password;
}
