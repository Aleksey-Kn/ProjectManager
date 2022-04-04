package ru.manager.ProgectManager.DTO.response.accessProject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.accessProject.CustomProjectRole;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Список всех кастомных ролей проекта")
public class CustomProjectRoleResponseList {
    @Schema(description = "Список ролей")
    private final List<CustomProjectRoleResponse> roles;

    public CustomProjectRoleResponseList(Set<CustomProjectRole> roleSet){
        roles = roleSet.stream().map(CustomProjectRoleResponse::new).collect(Collectors.toList());
    }
}
