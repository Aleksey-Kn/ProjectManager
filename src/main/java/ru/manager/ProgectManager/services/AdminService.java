package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.manager.ProgectManager.DTO.request.adminAction.LockRequest;
import ru.manager.ProgectManager.DTO.response.user.UserDataForAdmin;
import ru.manager.ProgectManager.DTO.response.user.UserDataForAdminList;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.user.NoSuchUserException;
import ru.manager.ProgectManager.repositories.UserRepository;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final MailService mailService;

    @Transactional
    public void lockAccount(LockRequest lockRequest) {
        User blockingUser = findOnIdOrLogin(lockRequest.getIdOrLogin()).orElseThrow(NoSuchUserException::new);
        if(blockingUser.getUserWithRoleConnectors().parallelStream().noneMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
            blockingUser.setAccountNonLocked(false);
            blockingUser = userRepository.save(blockingUser);
            if(lockRequest.getCause() != null && !lockRequest.getCause().isBlank()) {
                mailService.sendAboutLockAccount(blockingUser, lockRequest.getCause());
            }
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void unlockAccount(String idOrLogin) {
        Optional<User> user = findOnIdOrLogin(idOrLogin);
        if(user.isPresent()) {
            user.get().setAccountNonLocked(true);
            mailService.sendAboutUnlockAccount(userRepository.save(user.get()));
        } else {
            throw new NoSuchUserException();
        }
    }

    public UserDataForAdminList findAllUser(int zoneId) {
        return new UserDataForAdminList(StreamSupport.stream(userRepository.findAll().spliterator(), true)
                .filter(User::isEnabled)
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
