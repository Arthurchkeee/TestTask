package by.bank.dto.entityDto;

import by.bank.entity.EmailData;
import jakarta.validation.constraints.*;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link EmailData}
 */
@Value
public class EmailDataDto implements Serializable {
    @NotNull
    @Positive
    Long id;
    @Size(max = 200)
    @Email
    @NotBlank
    String email;
}