package ru.manager.ProgectManager.DTO.request.kanban;

import lombok.Data;

@Data
public class TagRequest {
    private String text;
    private String color;
}
