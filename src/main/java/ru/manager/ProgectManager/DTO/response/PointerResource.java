package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.manager.ProgectManager.enums.ResourceType;

@RequiredArgsConstructor
public class PointerResource {
    @Getter
    private final long id;
    @Getter
    private final String name;
    private final ResourceType resourceType;

    public String getResourceType() {
        return resourceType.getStringValue();
    }
}
