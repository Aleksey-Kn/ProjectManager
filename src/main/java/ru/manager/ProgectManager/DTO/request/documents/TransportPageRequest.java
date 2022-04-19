package ru.manager.ProgectManager.DTO.request.documents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Data
@Validated
@Schema(description = "Запрос на перемещение страницы документа")
public class TransportPageRequest {
    @Schema(description = "Идентификатор перемещаемой страницы", required = true)
    private long id;
    @Schema(description = "Идентификатор страницы, в подстраницы которой будет добавлена указанная страница")
    private long newParentId;
    @Min(value = 0, message = "INDEX_MUST_BE_MORE_0")
    @Schema(description = "Желаемый индекс в целевой деректории", required = true)
    private short index;
}
