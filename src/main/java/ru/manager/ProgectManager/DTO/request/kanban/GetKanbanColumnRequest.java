package ru.manager.ProgectManager.DTO.request.kanban;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Data
@Validated
public class GetKanbanColumnRequest {
    private long id;
    @Min(value = 0, message = "INDEX_MUST_BE_MORE_0")
    private int pageIndex;
    @Min(value = 1, message = "COUNT_MUST_BE_MORE_1")
    private int count;
}
