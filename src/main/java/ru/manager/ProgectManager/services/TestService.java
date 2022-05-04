package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.entitys.user.WorkTrack;
import ru.manager.ProgectManager.repositories.*;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final UserWithProjectConnectorRepository connectorRepository;
    private final KanbanElementRepository elementRepository;
    private final WorkTrackRepository workTrackRepository;

    public boolean removeUser(String idOrLogin) {
        Optional<User> user = Optional.ofNullable(userRepository.findByUsername(idOrLogin));
        if(user.isEmpty()) {
            long id;
            try{
                id = Long.parseLong(idOrLogin);
            } catch (Exception e) {
                return false;
            }
            user = userRepository.findById(id);
        }
        if(user.isPresent()) {
            for(UserWithProjectConnector connector: user.get().getUserWithProjectConnectors()) {
                connector.getProject().getConnectors().remove(connector);
                user.get().getUserWithProjectConnectors().remove(connector);
                projectRepository.save(connector.getProject());
                connectorRepository.delete(connector);
            }
            for(WorkTrack workTrack: user.get().getWorkTrackSet()) {
                workTrack.getTask().getWorkTrackSet().remove(workTrack);
                user.get().getWorkTrackSet().remove(workTrack);
                elementRepository.save(workTrack.getTask());
                workTrackRepository.delete(workTrack);
            }
            userRepository.delete(userRepository.save(user.get()));
            return true;
        } else {
            return false;
        }
    }
}
