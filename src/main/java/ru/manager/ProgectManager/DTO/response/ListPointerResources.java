package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ListPointerResources {
    private final List<PointerResource> resources;
}
