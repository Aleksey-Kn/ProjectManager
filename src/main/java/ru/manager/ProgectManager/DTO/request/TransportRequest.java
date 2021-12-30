package ru.manager.ProgectManager.DTO.request;

import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Getter
@Validated
public class TransportRequest {
    private long id;
    @Min(value = 0, message = "Index must be more 0")
    private int to;
}
