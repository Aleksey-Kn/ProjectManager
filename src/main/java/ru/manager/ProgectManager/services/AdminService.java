package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.repositories.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;

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
}
