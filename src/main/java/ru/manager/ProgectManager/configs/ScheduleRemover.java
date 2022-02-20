package ru.manager.ProgectManager.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import ru.manager.ProgectManager.entitys.TimeRemover;
import ru.manager.ProgectManager.exception.IncorrectStatusException;
import ru.manager.ProgectManager.repositories.TimeRemoverRepository;
import ru.manager.ProgectManager.services.ArchiveAndTrashService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Configuration
@EnableScheduling
public class ScheduleRemover {
    private TimeRemoverRepository timeRemoverRepository;
    @Autowired
    public void setTimeRemoverRepository(TimeRemoverRepository timeRemoverRepository){
        this.timeRemoverRepository = timeRemoverRepository;
    }

    private ArchiveAndTrashService trashService;
    @Autowired
    public void setTrashService(ArchiveAndTrashService archiveAndTrashService){
        trashService = archiveAndTrashService;
    }

    @Scheduled(fixedDelay = 86_400_000)
    public void remover(){
        List<Long> removersIndex = StreamSupport.stream(timeRemoverRepository.findAll().spliterator(), true)
                .filter(r -> LocalDate.ofEpochDay(r.getTimeToDelete()).isBefore(LocalDate.now()))
                .map(TimeRemover::getRemoverId)
                .collect(Collectors.toList());
        for(long now: removersIndex){
            try{
                trashService.finalDeleteElementFromTrash(now);
                timeRemoverRepository.deleteById(now);
            } catch (Exception ignored){}
        }
    }
}
