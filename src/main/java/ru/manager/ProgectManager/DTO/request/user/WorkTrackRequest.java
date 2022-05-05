package ru.manager.ProgectManager.DTO.request.user;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@Validated
public class WorkTrackRequest {
    private long taskId;
    @Min(value = 1, message = "COUNT_MUST_BE_MORE_1")
    private int workTime;
    @NotBlank(message = "TEXT_MUST_BE_CONTAINS_VISIBLE_SYMBOL")
    private String comment;
}
