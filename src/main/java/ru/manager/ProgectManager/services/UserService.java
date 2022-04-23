package ru.manager.ProgectManager.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.RefreshUserDTO;
import ru.manager.ProgectManager.DTO.request.UserDTO;
import ru.manager.ProgectManager.DTO.response.PointerResource;
import ru.manager.ProgectManager.entitys.*;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithDocumentConnector;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.enums.ResourceType;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.EmailAlreadyUsedException;
import ru.manager.ProgectManager.repositories.ApproveEnabledUserRepository;
import ru.manager.ProgectManager.repositories.RoleRepository;
import ru.manager.ProgectManager.repositories.UserRepository;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private RoleRepository roleRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private MailService mailService;
    private ApproveEnabledUserRepository approveEnabledUserRepository;

    public boolean saveUser(UserDTO userDTO) {
        if (userRepository.findByUsername(userDTO.getLogin()) == null) {
            if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
                throw new EmailAlreadyUsedException();
            }

            Role role = roleRepository.findByName("ROLE_USER");
            User user = new User();
            user.setUsername(userDTO.getLogin());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setEmail(userDTO.getEmail());
            user.setNickname(userDTO.getNickname());
            user.setUserWithRoleConnectors(Collections.singleton(role));
            user.setEnabled(false);
            user = userRepository.save(user);
            try {
                mailService.sendEmailApprove(user);
            } catch (MailException e) {
                userRepository.delete(user);
                throw e;
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean enabledUser(String token) {
        Optional<ApproveEnabledUser> approveEnabledUser = approveEnabledUserRepository.findById(token);
        if(approveEnabledUser.isPresent()) {
            User user = approveEnabledUser.get().getUser();
            user.setEnabled(true);
            userRepository.save(user);
            approveEnabledUserRepository.delete(approveEnabledUser.get());
            return true;
        } else {
            return false;
        }
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    public Optional<User> findById(long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsernameOrEmailAndPassword(String loginOrEmail, String password) {
        User user = userRepository.findByUsername(loginOrEmail);
        if (user != null) {
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        } else {
            Optional<User> u = userRepository.findByEmail(loginOrEmail);
            if (u.isPresent() && passwordEncoder.matches(password, u.get().getPassword())) {
                return u;
            }
        }
        return Optional.empty();
    }

    public boolean refreshUserData(String login, RefreshUserDTO userDTO) {
        User user = userRepository.findByUsername(login);
        if (passwordEncoder.matches(userDTO.getOldPassword(), user.getPassword())) {
            user.setNickname(userDTO.getNickname());
            user.setEmail(userDTO.getEmail());
            user.setPassword(passwordEncoder.encode(userDTO.getNewPassword()));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public void setPhoto(String login, byte[] file, String filename) throws IOException {
        User user = userRepository.findByUsername(login);
        if (user != null) {
            user.setPhoto(file);
            user.setContentTypePhoto(new MimetypesFileTypeMap().getContentType(filename));
            userRepository.save(user);
        }
    }

    public List<Project> allProjectOfThisUser(String login) {
        return userRepository.findByUsername(login).getUserWithProjectConnectors().stream()
                .map(UserWithProjectConnector::getProject)
                .collect(Collectors.toList());
    }

    public List<Project> projectsByNameOfThisUser(String name, String userLogin) {
        return userRepository.findByUsername(userLogin).getUserWithProjectConnectors().stream()
                .map(UserWithProjectConnector::getProject)
                .filter(p -> p.getName().toLowerCase(Locale.ROOT).contains(name))
                .collect(Collectors.toList());
    }

    public List<PointerResource> availableResourceByName(String name, String userLogin) {
        Set<UserWithProjectConnector> connectors =
                userRepository.findByUsername(userLogin).getUserWithProjectConnectors();
        List<PointerResource> result = connectors.stream()
                .flatMap(connector -> (connector.getRoleType() == TypeRoleProject.CUSTOM_ROLE
                        ? connector.getCustomProjectRole().getCustomRoleWithKanbanConnectors().parallelStream()
                        .map(CustomRoleWithKanbanConnector::getKanban)
                        : connector.getProject().getKanbans().stream()))
                .filter(kanban -> kanban.getName().toLowerCase().contains(name))
                .map(k -> new PointerResource(k.getId(), k.getName(), ResourceType.KANBAN))
                .collect(Collectors.toCollection(LinkedList::new));
        result.addAll(connectors.stream()
                .flatMap(connector -> (connector.getRoleType() == TypeRoleProject.CUSTOM_ROLE
                        ? connector.getProject().getPages().parallelStream()
                        .filter(page -> connector.getCustomProjectRole().getCustomRoleWithDocumentConnectors().stream()
                                .map(CustomRoleWithDocumentConnector::getPage)
                                .anyMatch(root -> root.equals(page) || root.equals(page.getRoot())))
                        : connector.getProject().getPages().stream()))
                .filter(section -> section.getName().toLowerCase().contains(name))
                .map(s -> new PointerResource(s.getId(), s.getName(), ResourceType.DOCUMENT))
                .collect(Collectors.toList()));
        return result;
    }

    public List<VisitMark> lastVisits(String userLogin) {
        return userRepository.findByUsername(userLogin).getVisitMarks().stream()
                .sorted(Comparator.comparing(VisitMark::getSerialNumber))
                .limit(20)
                .collect(Collectors.toList());
    }

    @Autowired
    private void setPasswordEncoder(PasswordEncoder p) {
        passwordEncoder = p;
    }

    @Autowired
    private void setRoleRepository(RoleRepository r) {
        roleRepository = r;
    }

    @Autowired
    private void setUserRepository(UserRepository u) {
        userRepository = u;
    }

    @Autowired
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    @Autowired
    public void setApproveEnabledUserRepository(ApproveEnabledUserRepository approveEnabledUserRepository) {
        this.approveEnabledUserRepository = approveEnabledUserRepository;
    }
}
