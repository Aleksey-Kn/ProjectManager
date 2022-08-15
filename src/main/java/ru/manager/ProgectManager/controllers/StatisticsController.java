package ru.manager.ProgectManager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.entitys.user.StatisticsUsing;
import ru.manager.ProgectManager.repositories.StatisticsRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Tag(name = "Сбор статистики о пользователях")
public class StatisticsController {
    private final StatisticsRepository statisticsRepository;

    @Operation(summary = "Сбор информации о целях использования приложения пользователем")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Статистика успешно добавлена"))
    @PostMapping("/users/statistics")
    public void setStatistics(@RequestBody List<String> answers){
        for(String answer: answers) {
            Optional<StatisticsUsing> statisticsUsing = Optional.
                    ofNullable(statisticsRepository.findByType(answer));
            statisticsUsing.ifPresentOrElse(s -> {
                s.setCount(s.getCount() + 1);
                statisticsRepository.save(s);
            }, () -> {
                StatisticsUsing su = new StatisticsUsing();
                su.setCount(1);
                su.setType(answer);
                statisticsRepository.save(su);
            });
        }
    }
}
