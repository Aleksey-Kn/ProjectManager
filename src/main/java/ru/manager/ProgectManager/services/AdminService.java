package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.adminAction.LockRequest;
import ru.manager.ProgectManager.DTO.response.user.UserDataForAdmin;
import ru.manager.ProgectManager.DTO.response.user.UserDataForAdminList;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.repositories.UserRepository;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final MailService mailService;

    public boolean lockAccount(LockRequest lockRequest) {
        User blockingUser = findOnIdOrLogin(lockRequest.getIdOrLogin()).orElseThrow();
        if(blockingUser.getUserWithRoleConnectors().parallelStream().noneMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
            blockingUser.setAccountNonLocked(false);
            if(lockRequest.getCause() != null && !lockRequest.getCause().isBlank()) {
                mailService.sendAboutLockAccount(userRepository.save(blockingUser), lockRequest.getCause());
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean unlockAccount(String idOrLogin) {
        Optional<User> user = findOnIdOrLogin(idOrLogin);
        if(user.isPresent()) {
            user.get().setAccountNonLocked(true);
            mailService.sendAboutUnlockAccount(userRepository.save(user.get()));
            return true;
        } else {
            return false;
        }
    }

    public UserDataForAdminList findAllUser(int zoneId) {
        return new UserDataForAdminList(StreamSupport.stream(userRepository.findAll().spliterator(), true)
                .map(user -> new UserDataForAdmin(user, zoneId))
                .collect(Collectors.toList()));
    }

    private Optional<User> findOnIdOrLogin(String idOrLogin) {
        Optional<User> user = Optional.ofNullable(userRepository.findByUsername(idOrLogin));
        if(user.isEmpty()) {
            long id;
            try{
                id = Long.parseLong(idOrLogin);
            } catch (Exception e) {
                return Optional.empty();
            }
            user = userRepository.findById(id);
        }
        return user;
    }
}
