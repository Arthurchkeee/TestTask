package by.bank.dto.entityDto;

import by.bank.entity.PhoneData;
import jakarta.validation.constraints.*;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link PhoneData}
 */
@Value
public class PhoneDataDto implements Serializable {
    @NotNull
    @Positive
    Long id;
    @Size(max = 13)
    @NotBlank
    String phone;
}