package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Getter
@Validated
@Schema(description = "Запрос на установку автоматической очистки колонки канбан доски")
public class DelayRemoveRequest {
    @Schema(description = "Идентификатор колонки", required = true)
    private long id;
    @Min(value = 1)
    @Schema(description = "Время жизни элементов в столбце в днях до того, как они будут перемещены в корзину")
    private int delay;
}
