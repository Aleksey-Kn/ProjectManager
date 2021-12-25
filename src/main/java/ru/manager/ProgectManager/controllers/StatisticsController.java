package ru.manager.ProgectManager.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.entitys.StatisticsUsing;
import ru.manager.ProgectManager.repositories.StatisticsRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsRepository statisticsRepository;

    @PostMapping("/users/set_statistics")
    public ResponseEntity<?> setStatistics(@RequestBody List<String> answers){
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
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
