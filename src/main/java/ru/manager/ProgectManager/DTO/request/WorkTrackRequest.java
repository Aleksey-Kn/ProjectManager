package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
@Schema(description = "Запрос на получение данных о количестве работы пользователя в указанном проекте за указанный период")
public class WorkTrackRequest {
    @NotBlank(message = "FIELD_MUST_BE_NOT_NULL")
    @Schema(description = "Дата начала запрашиваемого временного интервала")
    private String fromDate;
    @NotBlank(message = "FIELD_MUST_BE_NOT_NULL")
    @Schema(description = "Дата конца запрашиваемого временного интервала")
    private String toDate;
    @Schema(description = "Идентификатор проекта, в котром производилась работа", required = true)
    private long projectId;
    @Schema(description =
            "Идентификатор пользователя, о котором требуется получить информцию. При отсутствии загружается инфоормация о текущем пользователе")
    private long userId;
}
