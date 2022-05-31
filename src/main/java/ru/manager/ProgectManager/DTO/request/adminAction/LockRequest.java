package ru.manager.ProgectManager.DTO.request.adminAction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
@Schema(description = "Данные для запроса блокирования пользователя")
public class LockRequest {
    @NotBlank
    @Schema(description = "Идентификатор блокируемого пользователя")
    private String idOrLogin;
    @Schema(description = "Причина блокировки пользователя")
    private String cause;
}
