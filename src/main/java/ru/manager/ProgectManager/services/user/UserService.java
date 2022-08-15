package ru.manager.ProgectManager.services.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.manager.ProgectManager.DTO.UserDetailsDTO;
import ru.manager.ProgectManager.DTO.request.user.AuthDto;
import ru.manager.ProgectManager.DTO.request.user.LocaleRequest;
import ru.manager.ProgectManager.DTO.request.user.RegisterUserDTO;
import ru.manager.ProgectManager.DTO.response.ListPointerResources;
import ru.manager.ProgectManager.DTO.response.PointerResource;
import ru.manager.ProgectManager.DTO.response.project.ProjectResponse;
import ru.manager.ProgectManager.DTO.response.user.MyselfUserDataResponse;
import ru.manager.ProgectManager.DTO.response.user.PublicAllDataResponse;
import ru.manager.ProgectManager.DTO.response.user.VisitMarkResponse;
import ru.manager.ProgectManager.components.LocalisedMessages;
import ru.manager.ProgectManager.components.PhotoCompressor;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.ScheduledMailInfo;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithDocumentConnector;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.user.*;
import ru.manager.ProgectManager.enums.ActionType;
import ru.manager.ProgectManager.enums.Size;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.runtime.EmailAlreadyUsedException;
import ru.manager.ProgectManager.exception.user.AccountIsLockedException;
import ru.manager.ProgectManager.exception.user.AccountIsNotEnabledException;
import ru.manager.ProgectManager.exception.user.IncorrectLoginOrPasswordException;
import ru.manager.ProgectManager.exception.user.NoSuchUserException;
import ru.manager.ProgectManager.repositories.*;
import ru.manager.ProgectManager.services.MailService;
import ru.manager.ProgectManager.services.project.ProjectService;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserService {
    private RoleRepository roleRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private MailService mailService;
    private ApproveActionTokenRepository approveActionTokenRepository;
    private UsedAddressRepository usedAddressRepository;
    private NotificationService notificationService;
    private PhotoCompressor compressor;
    private RefreshTokenRepository refreshTokenRepository;
    private LocalisedMessages localisedMessages;
    private ScheduledMailInfoRepository mailInfoRepository;
    private ProjectService projectService;

    @Transactional
    public Optional<String> saveUser(RegisterUserDTO registerUserDTO) {
        if (userRepository.findByUsername(registerUserDTO.getLogin()) == null) {
            if (userRepository.findByEmail(registerUserDTO.getEmail()).isPresent()) {
                throw new EmailAlreadyUsedException();
            }
            Role role = roleRepository.findByName("ROLE_USER");
            User user = new User();
            user.setUsername(registerUserDTO.getLogin().trim());
            user.setPassword(passwordEncoder.encode(registerUserDTO.getPassword()));
            user.setEmail(registerUserDTO.getEmail());
            user.setNickname(registerUserDTO.getNickname());
            user.setUserWithRoleConnectors(Collections.singleton(role));
            user.setEnabled(false);
            user.setAccountNonLocked(true);
            user.setLocale(registerUserDTO.getLocale());
            user.setLastVisit(0);
            user.setZoneId(Integer.parseInt(registerUserDTO.getZoneId()));
            user = userRepository.save(user);
            try {
                ScheduledMailInfo scheduledMailInfo = new ScheduledMailInfo();
                scheduledMailInfo.setUserEmail(user.getEmail());
                scheduledMailInfo.setSubject(localisedMessages.buildSubjectAboutCompletionOfRegistration(user.getLocale()));
                scheduledMailInfo.setText(localisedMessages.buildTextAboutCompletionOfRegistration(user.getLocale()) +
                        mailService.sendEmailApprove(user, registerUserDTO.getUrl(), registerUserDTO.getLocale()));
                mailInfoRepository.save(scheduledMailInfo);
            } catch (MailException e) {
                userRepository.delete(user);
                throw e;
            }

            return Optional.of(user.getUsername());
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    public void updateLastVisitAndZone(String username, int zoneId) {
        User user = userRepository.findByUsername(username);
        user.setLastVisit(LocalDateTime.now()
                .toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())));
        user.setZoneId(zoneId);
        userRepository.save(user);
    }

    @Transactional
    public Optional<String> enabledUser(String token) {
        Optional<ApproveActionToken> approveEnabledUser = approveActionTokenRepository.findById(token);
        if (approveEnabledUser.isPresent() && approveEnabledUser.get().getActionType() == ActionType.APPROVE_ENABLE) {
            User user = approveEnabledUser.get().getUser();
            user.setEnabled(true);
            mailInfoRepository.deleteById(user.getEmail());
            approveActionTokenRepository.delete(approveEnabledUser.get());
            return Optional.of(userRepository.save(user).getUsername());
        } else {
            return Optional.empty();
        }
    }

    public boolean attemptDropPass(String loginOrEmail, String url) {
        Optional<User> user = findLoginOrEmail(loginOrEmail);
        if (user.isPresent()) {
            mailService.sendResetPass(user.get(), url);
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public boolean resetPass(String token, String newPassword) {
        Optional<ApproveActionToken> approveActionToken = approveActionTokenRepository.findById(token);
        if (approveActionToken.isPresent() && approveActionToken.get().getActionType() == ActionType.RESET_PASS) {
            User user = approveActionToken.get().getUser();
            refreshTokenRepository.deleteAllByLogin(user.getUsername());
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            approveActionTokenRepository.delete(approveActionToken.get());
            return true;
        } else {
            return false;
        }
    }

    public MyselfUserDataResponse findMyselfUserDataResponseByUsername(String username) {
        return new MyselfUserDataResponse(userRepository.findByUsername(username));
    }

    public UserDetailsDTO findUserDetailsByUsername(String username) {
        return new UserDetailsDTO(userRepository.findByUsername(username));
    }

    public PublicAllDataResponse findById(long id, String userLogin) throws NoSuchUserException {
        return new PublicAllDataResponse(userRepository.findById(id).orElseThrow(NoSuchUserException::new),
                findZoneIdForThisUser(userLogin));
    }

    @Transactional
    public String login(AuthDto authDto) throws AccountIsNotEnabledException, AccountIsLockedException, IncorrectLoginOrPasswordException {
        Optional<User> user = findLoginOrEmail(authDto.getLogin());
        if (user.isPresent() && passwordEncoder.matches(authDto.getPassword(), user.get().getPassword())) {
            if(!user.get().isEnabled())
                throw new AccountIsNotEnabledException();
            if(!user.get().isAccountNonLocked())
                throw new AccountIsLockedException();

            user.get().setZoneId(Integer.parseInt(authDto.getZoneId()));
            user.get().setLastVisit(LocalDateTime.now()
                    .toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())));
            if (user.get().getUsedAddresses().stream().map(UsedAddress::getIp).noneMatch(ip -> ip.equals(authDto.getIp()))) {
                mailService.sendAboutAuthorisation(user.get(), authDto.getIp(), authDto.getBrowser(),
                        authDto.getCountry(), authDto.getCity(), authDto.getZoneId());
                notificationService.addNotificationAboutAuthorisation(authDto, user.get());

                UsedAddress usedAddress = new UsedAddress();
                usedAddress.setIp(authDto.getIp());
                user.get().getUsedAddresses().add(usedAddressRepository.save(usedAddress));
            }
            return userRepository.save(user.get()).getUsername();
        } else {
            throw new IncorrectLoginOrPasswordException();
        }
    }

    @Transactional
    public void renameUser(String login, String newName) {
        User user = userRepository.findByUsername(login);
        user.setNickname(newName.trim());
        userRepository.save(user);
    }

    @Transactional
    public void updatePass(String oldPass, String newPass, String userLogin) throws IncorrectLoginOrPasswordException {
        User user = userRepository.findByUsername(userLogin);
        if (passwordEncoder.matches(oldPass, user.getPassword())) {
            refreshTokenRepository.deleteAllByLogin(user.getUsername());
            user.setPassword(passwordEncoder.encode(newPass));
            userRepository.save(user);
        } else {
            throw new IncorrectLoginOrPasswordException();
        }
    }

    @Transactional
    public void updateLocale(LocaleRequest localeRequest, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        user.setLocale(localeRequest.getLocale());
        userRepository.save(user);
    }

    @Transactional
    public void setPhoto(String login, MultipartFile multipartFile) throws IOException {
        User user = userRepository.findByUsername(login);
        if (user != null) {
            user.setPhoto(compressor.compress(multipartFile, Size.SMALL));
            userRepository.save(user);
        }
    }

    public byte[] findPhoto(long id) {
        return userRepository.findById(id).orElseThrow().getPhoto();
    }

    public List<ProjectResponse> allProjectOfThisUser(String login) {
        List<Project> projectList = userRepository.findByUsername(login).getUserWithProjectConnectors().stream()
                .map(UserWithProjectConnector::getProject)
                .collect(Collectors.toList());
        List<String> roles = new ArrayList<>();
        return createProjectResponseList(login, projectList, roles);
    }

    public List<ProjectResponse> projectsByNameOfThisUser(String inputName, String userLogin) {
        String name = inputName.trim().toLowerCase();
        List<Project> projectList = userRepository.findByUsername(userLogin).getUserWithProjectConnectors().stream()
                .map(UserWithProjectConnector::getProject)
                .filter(p -> p.getName().toLowerCase().contains(name))
                .collect(Collectors.toList());
        List<String> roles = new LinkedList<>();
        return createProjectResponseList(userLogin, projectList, roles);
    }

    private List<ProjectResponse> createProjectResponseList(String userLogin, List<Project> projectList, List<String> roles) {
        projectList.forEach(p -> roles.add(projectService.findUserRoleName(userLogin, p.getId())));
        List<ProjectResponse> responses = new LinkedList<>();
        final int zoneId = findZoneIdForThisUser(userLogin);
        for(int i = 0; i < projectList.size(); i++){
            responses.add(new ProjectResponse(projectList.get(i), roles.get(i), zoneId));
        }
        return responses;
    }

    public ListPointerResources availableResourceByName(String inputName, String userLogin) {
        String name = inputName.trim().toLowerCase();
        Set<UserWithProjectConnector> connectors = // получение подключений текущего пользователя
                userRepository.findByUsername(userLogin).getUserWithProjectConnectors();
        List<PointerResource> result = connectors.stream()
                .flatMap(connector -> (connector.getRoleType() == TypeRoleProject.CUSTOM_ROLE
                        ? connector.getCustomProjectRole().getCustomRoleWithKanbanConnectors().parallelStream()
                        .map(CustomRoleWithKanbanConnector::getKanban)
                        : connector.getProject().getKanbans().stream()))
                .filter(kanban -> kanban.getName().toLowerCase().contains(name))
                .map(PointerResource::new)
                .collect(Collectors.toCollection(LinkedList::new));
        result.addAll(connectors.stream()
                .flatMap(connector -> (connector.getRoleType() == TypeRoleProject.CUSTOM_ROLE
                        ? connector.getProject().getPages().parallelStream()
                        .filter(page -> connector.getCustomProjectRole().getCustomRoleWithDocumentConnectors().stream()
                                .map(CustomRoleWithDocumentConnector::getPage)
                                .anyMatch(root -> root.equals(page) || root.equals(page.getRoot())))
                        : connector.getProject().getPages().stream()))
                .filter(section -> section.getName().toLowerCase().contains(name))
                .map(PointerResource::new)
                .collect(Collectors.toList()));
        result.addAll(connectors.stream()
                .map(UserWithProjectConnector::getProject)
                .filter(section -> section.getName().toLowerCase().contains(name))
                .map(PointerResource::new)
                .collect(Collectors.toList()));
        return new ListPointerResources(result);
    }

    public List<VisitMarkResponse> lastVisits(String userLogin) {
        return userRepository.findByUsername(userLogin).getVisitMarks().stream()
                .sorted(Comparator.comparing(VisitMark::getSerialNumber))
                .limit(20)
                .map(VisitMarkResponse::new)
                .collect(Collectors.toList());
    }

    public int findZoneIdForThisUser(String userLogin) {
        return userRepository.findByUsername(userLogin).getZoneId();
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

    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Autowired
    public void setCompressor(PhotoCompressor compressor) {
        this.compressor = compressor;
    }

    @Autowired
    public void setRefreshTokenRepository(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Autowired
    public void setLocalisedMessages(LocalisedMessages localisedMessages) {
        this.localisedMessages = localisedMessages;
    }

    @Autowired
    public void setMailInfoRepository(ScheduledMailInfoRepository mailInfoRepository) {
        this.mailInfoRepository = mailInfoRepository;
    }

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }
}
