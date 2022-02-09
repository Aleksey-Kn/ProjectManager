package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.enums.Errors;

import java.util.Collections;
import java.util.List;

@Getter
@Schema(description = "Информация об ошибках текущего запроса")
public class ErrorResponse {
    @Schema(description = "Список ошибок")
    private final List<Integer> errors;

    public ErrorResponse(List<Integer> err){
        errors = err;
    }

    public ErrorResponse(Errors error){
        errors = Collections.singletonList(error.getNumValue());
    }
}
