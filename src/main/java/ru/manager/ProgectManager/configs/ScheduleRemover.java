package ru.manager.ProgectManager.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import ru.manager.ProgectManager.entitys.TimeRemover;
import ru.manager.ProgectManager.repositories.TimeRemoverRepository;
import ru.manager.ProgectManager.services.kanban.ArchiveAndTrashService;
import ru.manager.ProgectManager.services.kanban.KanbanElementService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Configuration
@EnableScheduling
public class ScheduleRemover {
    private TimeRemoverRepository timeRemoverRepository;
    private ArchiveAndTrashService trashService;
    private KanbanElementService elementService;

    @Scheduled(fixedDelay = 86_400_000)
    public void remover() {
        List<Long> removersIndex = StreamSupport
                .stream(timeRemoverRepository.findAll().spliterator(), true)
                .filter(r -> LocalDate.ofEpochDay(r.getTimeToDelete()).isBefore(LocalDate.now()))
                .filter(TimeRemover::isHard)
                .map(TimeRemover::getRemoverId)
                .collect(Collectors.toList());
        for (long now : removersIndex) {
            try {
                trashService.finalDeleteElementFromTrash(now);
                timeRemoverRepository.deleteById(now);
            } catch (Exception ignored) {
            }
        }
    }

    @Scheduled(fixedDelay = 85_000_000)
    public void utilization() {
        List<Long> utilizeIndex = StreamSupport
                .stream(timeRemoverRepository.findAll().spliterator(), true)
                .filter(r -> LocalDate.ofEpochDay(r.getTimeToDelete()).isBefore(LocalDate.now()))
                .filter(r -> !r.isHard())
                .map(TimeRemover::getRemoverId)
                .collect(Collectors.toList());

        for(long now: utilizeIndex){
            try {
                elementService.utiliseElementFromSystem(now);
                timeRemoverRepository.deleteById(now);
            } catch (Exception ignored){}
        }
    }

    @Autowired
    public void setTimeRemoverRepository(TimeRemoverRepository timeRemoverRepository) {
        this.timeRemoverRepository = timeRemoverRepository;
    }

    @Autowired
    public void setTrashService(ArchiveAndTrashService archiveAndTrashService) {
        trashService = archiveAndTrashService;
    }

    @Autowired
    public void setElementService(KanbanElementService elementService) {
        this.elementService = elementService;
    }
}
