package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.CustomProjectRole;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CustomProjectRoleResponseList {
    private final List<CustomProjectRoleResponse> roles;

    public CustomProjectRoleResponseList(Set<CustomProjectRole> roleSet){
        roles = roleSet.stream().map(CustomProjectRoleResponse::new).collect(Collectors.toList());
    }
}
