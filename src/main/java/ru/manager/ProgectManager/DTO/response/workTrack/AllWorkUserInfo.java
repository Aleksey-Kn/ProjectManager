package ru.manager.ProgectManager.DTO.response.workTrack;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class AllWorkUserInfo {
    private Set<ElementWithWorkResponse> tasks;
    private List<WorkTrackShortResponse> workInConcreteDay;
    private int summaryWorkInDiapason;
}
