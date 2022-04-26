package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.user.VisitMark;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Список последних посещённых пользователем ресурсов")
public class VisitMarkListResponse {
    @Schema(description = "Список информации о ресурсах")
    private final List<VisitMarkResponse> visitMarks;

    public VisitMarkListResponse(List<VisitMark> markList) {
        visitMarks = markList.stream().map(VisitMarkResponse::new).collect(Collectors.toList());
    }
}
