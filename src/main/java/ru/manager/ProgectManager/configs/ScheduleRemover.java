package ru.manager.ProgectManager.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import ru.manager.ProgectManager.entitys.ScheduledMailInfo;
import ru.manager.ProgectManager.entitys.kanban.TimeRemover;
import ru.manager.ProgectManager.repositories.ScheduledMailInfoRepository;
import ru.manager.ProgectManager.repositories.TimeRemoverRepository;
import ru.manager.ProgectManager.services.MailService;
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
    private ScheduledMailInfoRepository scheduledMailInfoRepository;
    private MailService mailService;

    @Scheduled(fixedDelay = 86_400_000)
    public void remover() {
        List<Long> removersIndex = StreamSupport
                .stream(timeRemoverRepository.findAll().spliterator(), true)
                .filter(r -> LocalDate.ofEpochDay(r.getTimeToDelete()).isBefore(LocalDate.now()))
                .filter(TimeRemover::isHard)
                .map(TimeRemover::getRemoverId)
                .collect(Collectors.toList());
        for (long now : removersIndex) {
            timeRemoverRepository.deleteById(now);
            try {
                trashService.finalDeleteElementFromTrash(now);
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

        for (long now : utilizeIndex) {
            try {
                elementService.utiliseElementFromSystem(now);
            } catch (Exception ignored) {
            }
        }
    }

    @Scheduled(fixedDelay = 3_600_000)
    public void scheduledMailSandler() {
        for(ScheduledMailInfo scheduledMailInfo: scheduledMailInfoRepository.findAll()) {
            mailService.send(scheduledMailInfo);
            if(scheduledMailInfo.isResend()) {
                scheduledMailInfoRepository.delete(scheduledMailInfo);
            } else {
                scheduledMailInfo.setResend(true);
                scheduledMailInfoRepository.save(scheduledMailInfo);
            }
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

    @Autowired
    public void setScheduledMailInfoRepository(ScheduledMailInfoRepository scheduledMailInfoRepository) {
        this.scheduledMailInfoRepository = scheduledMailInfoRepository;
    }

    @Autowired
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }
}
