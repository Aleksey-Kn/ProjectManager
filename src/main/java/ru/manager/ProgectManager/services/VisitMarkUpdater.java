package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.entitys.user.VisitMark;
import ru.manager.ProgectManager.enums.ResourceType;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.VisitMarkRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VisitMarkUpdater {
    private final VisitMarkRepository visitMarkRepository;
    private final UserRepository userRepository;

    public void updateVisitMarks(User user, long id, String name, ResourceType resourceType){
        Optional<VisitMark> visitMark = user.getVisitMarks().stream()
                .filter(v -> v.getResourceType() == resourceType && v.getResourceId() == id).findAny();
        if(visitMark.isPresent()) {
            user.getVisitMarks().stream()
                    .filter(vm -> vm.getSerialNumber() < visitMark.get().getSerialNumber())
                    .forEach(vm -> {
                        vm.setSerialNumber((byte) (vm.getSerialNumber() + 1));
                        visitMarkRepository.save(vm);
                    });
            visitMark.get().setSerialNumber((byte)0);
            visitMarkRepository.save(visitMark.get());
        } else {
            user.getVisitMarks().forEach(vm -> vm.setSerialNumber((byte) (vm.getSerialNumber() + 1)));
            user.getVisitMarks().removeIf(vm -> vm.getSerialNumber() > 120);
            VisitMark newMark = new VisitMark();
            newMark.setSerialNumber((byte) 0);
            newMark.setResourceId(id);
            newMark.setResourceName(name);
            newMark.setResourceType(resourceType);
            user.getVisitMarks().add(visitMarkRepository.save(newMark));
            userRepository.save(user);
        }
    }
}
