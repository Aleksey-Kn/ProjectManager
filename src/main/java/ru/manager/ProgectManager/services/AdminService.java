package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.adminAction.LockRequest;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.repositories.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final MailService mailService;

    public boolean lockAccount(LockRequest lockRequest) {
        User blockingUser = userRepository.findById(lockRequest.getId()).orElseThrow();
        if(blockingUser.getUserWithRoleConnectors().parallelStream().noneMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
            blockingUser.setAccountNonLocked(false);
            mailService.sendAboutLockAccount(userRepository.save(blockingUser), lockRequest.getCause());
            return true;
        } else {
            return false;
        }
    }

    public boolean unlockAccount(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()) {
            user.get().setAccountNonLocked(true);
            mailService.sendAboutUnlockAccount(userRepository.save(user.get()));
            return true;
        } else {
            return false;
        }
    }
}
