package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.repositories.ProjectRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.UserWithProjectConnectorRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final UserWithProjectConnectorRepository connectorRepository;

    public boolean lockAccount(long userId) {
        User blockingUser = userRepository.findById(userId).orElseThrow();
        if(blockingUser.getUserWithRoleConnectors().parallelStream().noneMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
            blockingUser.setAccountNonLocked(false);
            userRepository.save(blockingUser);
            return true;
        } else {
            return false;
        }
    }

    public boolean unlockAccount(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()) {
            user.get().setAccountNonLocked(true);
            userRepository.save(user.get());
            return true;
        } else {
            return false;
        }
    }

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
            user.get().getUserWithRoleConnectors().clear();
            for(UserWithProjectConnector connector: user.get().getUserWithProjectConnectors()) {
                connector.getProject().getConnectors().remove(connector);
                user.get().getUserWithProjectConnectors().remove(connector);
                projectRepository.save(connector.getProject());
                connectorRepository.delete(connector);
            }
            userRepository.delete(userRepository.save(user.get()));
            return true;
        } else {
            return false;
        }
    }
}
