package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.manager.ProgectManager.entitys.VisitMark;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Schema(description = "Список последних посещённых пользователем ресурсов")
public class VisitMarksResponse {
    @Schema(description = "Список информации о ресурсах")
    private final List<VisitMark> visitMarks;
}
