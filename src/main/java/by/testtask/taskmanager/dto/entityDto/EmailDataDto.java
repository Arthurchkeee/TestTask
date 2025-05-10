package by.testtask.taskmanager.dto.entityDto;

import jakarta.validation.constraints.*;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link by.testtask.taskmanager.entity.EmailData}
 */
@Value
public class EmailDataDto implements Serializable {
    @NotNull
    @Positive
    Long id;
    @NotNull
    @Size(max = 200)
    @Email
    @NotEmpty
    @NotBlank
    String email;
}