package ru.manager.ProgectManager.DTO.request.documents;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Data
@Validated
public class TransportPageRequest {
    private long id;
    private long newParentId;
    @Min(value = 0, message = "INDEX_MUST_BE_MORE_0")
    private short index;
}
