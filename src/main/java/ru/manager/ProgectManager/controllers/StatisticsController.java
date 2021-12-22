package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.DTO.UserTargetRequest;
import ru.manager.ProgectManager.entitys.StatisticsUsing;
import ru.manager.ProgectManager.repositories.StatisticsRepository;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsRepository statisticsRepository;

    @PostMapping("/user/set_statistics")
    public ResponseEntity<?> setStatistics(@RequestBody UserTargetRequest request){
        Optional<StatisticsUsing> statisticsUsing = Optional.
                ofNullable(statisticsRepository.findByType(request.getTarget()));
        statisticsUsing.ifPresentOrElse(s -> {
            s.setCount(s.getCount() + 1);
            statisticsRepository.save(s);
        }, () -> {
            StatisticsUsing su = new StatisticsUsing();
            su.setCount(1);
            su.setType(request.getTarget());
            statisticsRepository.save(su);
        });
        return ResponseEntity.ok("OK");
    }
}
