package by.testtask.taskmanager.dto.entityDto;

import jakarta.validation.constraints.*;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link by.testtask.taskmanager.entity.PhoneData}
 */
@Value
public class PhoneDataDto implements Serializable {
    @NotNull
    @Positive
    Long id;
    @NotNull
    @Size(max = 13)
    @NotEmpty
    @NotBlank
    String phone;
}