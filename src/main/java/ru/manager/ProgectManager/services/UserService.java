package ru.manager.ProgectManager.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.user.AuthDto;
import ru.manager.ProgectManager.DTO.request.user.RegisterUserDTO;
import ru.manager.ProgectManager.DTO.response.PointerResource;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithDocumentConnector;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.user.*;
import ru.manager.ProgectManager.enums.ActionType;
import ru.manager.ProgectManager.enums.ResourceType;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.EmailAlreadyUsedException;
import ru.manager.ProgectManager.repositories.ApproveActionTokenRepository;
import ru.manager.ProgectManager.repositories.RoleRepository;
import ru.manager.ProgectManager.repositories.UsedAddressRepository;
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
    private ApproveActionTokenRepository approveActionTokenRepository;
    private UsedAddressRepository usedAddressRepository;

    public boolean saveUser(RegisterUserDTO registerUserDTO) {
        if (userRepository.findByUsername(registerUserDTO.getLogin()) == null) {
            if (userRepository.findByEmail(registerUserDTO.getEmail()).isPresent()) {
                throw new EmailAlreadyUsedException();
            }

            Role role = roleRepository.findByName("ROLE_USER");
            User user = new User();
            user.setUsername(registerUserDTO.getLogin());
            user.setPassword(passwordEncoder.encode(registerUserDTO.getPassword()));
            user.setEmail(registerUserDTO.getEmail());
            user.setNickname(registerUserDTO.getNickname());
            user.setUserWithRoleConnectors(Collections.singleton(role));
            user.setEnabled(false);
            user.setAccountNonLocked(true);
            user = userRepository.save(user);
            try {
                mailService.sendEmailApprove(user, registerUserDTO.getUrl(), registerUserDTO.getLocale());
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
        Optional<ApproveActionToken> approveEnabledUser = approveActionTokenRepository.findById(token);
        if (approveEnabledUser.isPresent() && approveEnabledUser.get().getActionType() == ActionType.APPROVE_ENABLE) {
            User user = approveEnabledUser.get().getUser();
            user.setEnabled(true);
            userRepository.save(user);
            approveActionTokenRepository.delete(approveEnabledUser.get());
            return true;
        } else {
            return false;
        }
    }

    public boolean attemptDropPass(String loginOrEmail, String url, ru.manager.ProgectManager.enums.Locale locale) {
        Optional<User> user = findLoginOrEmail(loginOrEmail);
        if(user.isPresent()) {
            mailService.sendResetPass(user.get(), url, locale);
            return true;
        } else {
            return false;
        }
    }

    public boolean resetPass(String token, String newPassword) {
        Optional<ApproveActionToken> approveActionToken = approveActionTokenRepository.findById(token);
        if(approveActionToken.isPresent() && approveActionToken.get().getActionType() == ActionType.RESET_PASS) {
            User user = approveActionToken.get().getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            approveActionTokenRepository.delete(approveActionToken.get());
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

    public Optional<User> login(AuthDto authDto) {
        Optional<User> user = findLoginOrEmail(authDto.getLogin());
        if (user.isPresent() && passwordEncoder.matches(authDto.getPassword(), user.get().getPassword())) {
            if(user.get().getUsedAddresses().stream().map(UsedAddress::getIp).noneMatch(ip -> ip.equals(authDto.getIp()))){
                mailService.sendAboutAuthorisation(user.get().getEmail(), authDto.getIp(), authDto.getBrowser(),
                        authDto.getCountry(), authDto.getCity(), authDto.getZoneId(), authDto.getLocale());
                UsedAddress usedAddress = new UsedAddress();
                usedAddress.setIp(authDto.getIp());
                user.get().getUsedAddresses().add(usedAddressRepository.save(usedAddress));
                return Optional.of(userRepository.save(user.get()));
            } else {
                return user;
            }
        }
        return Optional.empty();
    }

    public void renameUser(String login, String newName) {
        User user = userRepository.findByUsername(login);
        user.setNickname(newName);
        userRepository.save(user);
    }

    public boolean updatePass(String oldPass, String newPass, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        if(passwordEncoder.matches(oldPass, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPass));
            userRepository.save(user);
            return true;
        } else {
            return false;
        }
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

    private Optional<User> findLoginOrEmail(String loginOrEmail) {
        Optional<User> user = Optional.ofNullable(userRepository.findByUsername(loginOrEmail));
        if (user.isPresent()) {
            return user;
        } else {
            return userRepository.findByEmail(loginOrEmail);
        }
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
    public void setApproveEnabledUserRepository(ApproveActionTokenRepository approveActionTokenRepository) {
        this.approveActionTokenRepository = approveActionTokenRepository;
    }

    @Autowired
    public void setUsedAddressRepository(UsedAddressRepository usedAddressRepository) {
        this.usedAddressRepository = usedAddressRepository;
    }
}
