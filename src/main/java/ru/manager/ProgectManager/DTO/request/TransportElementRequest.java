package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Validated
@Getter
@Schema(description = "Запрос на передвижение елемента канбана")
public class TransportElementRequest {
    @Schema(required = true, description = "Идентификатор передвигаемого элемента")
    private long id;
    @Schema(description = "Новый индекс указанного элемента")
    @Min(value = 0, message = "Index must be more 0")
    private int toIndex;
    @Min(value = 0, message = "Index must be more 0")
    @Schema(description = "Индентификатор целевой колонки элемента")
    private int toColumn;
}
