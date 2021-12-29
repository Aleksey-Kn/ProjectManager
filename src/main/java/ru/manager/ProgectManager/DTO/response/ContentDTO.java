package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ContentDTO {
    private final String content;
    private final byte[] photo;
}
