package ru.manager.ProgectManager.DTO.request;

import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Validated
@Getter
public class TransportElementRequest {
    private long id;
    @Min(value = 0, message = "Index must be more 0")
    private int toIndex;
    @Min(value = 0, message = "Index must be more 0")
    private int toColumn;
}
