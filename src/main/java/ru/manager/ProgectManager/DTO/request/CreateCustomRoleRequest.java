package ru.manager.ProgectManager.DTO.request;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Validated
public class CreateCustomRoleRequest {
    private long projectId;
    @NotBlank
    private String name;
    private boolean canEditResource;
    private List<KanbanConnectorRequest> kanbanConnectorRequests;
}
