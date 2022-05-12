package ru.manager.ProgectManager.DTO.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
@Schema(description = "Данные для создания или изменения записки к аккаунту пользователя")
public class NoteRequest {
    @Schema(description = "Идентификатор пользователя, к которому будет прикреплена записка", required = true)
    private long targetUserId;
    @NotBlank
    @Schema(description = "Текст записки")
    private String text;
}
