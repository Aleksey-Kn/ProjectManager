package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Data
@Validated
@Schema(description = "Запрос на получение ресурса с пагинированием")
public class GetResourceWithPagination {
    @Schema(description = "Идентификатор получаемого ресурса")
    private long id;
    @Min(value = 0, message = "INDEX_MUST_BE_MORE_0")
    @Schema(description = "Первый порядковый номер в запрашиваемой странице")
    private int pageIndex;
    @Min(value = 1, message = "COUNT_MUST_BE_MORE_1")
    @Schema(description = "Количество элементов в запрашиваемой странице")
    private int count;
}
