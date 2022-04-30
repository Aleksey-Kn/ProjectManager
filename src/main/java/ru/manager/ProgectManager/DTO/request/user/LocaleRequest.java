package ru.manager.ProgectManager.DTO.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import ru.manager.ProgectManager.enums.Locale;

import javax.validation.constraints.NotNull;

@Data
@Validated
@Schema(description = "Данные запроса на смену языка аккаунта пользователя")
public class LocaleRequest {
    @NotNull
    @Schema(description = "Язык письма, отправляемого на почту пользователю")
    private Locale locale;
}
