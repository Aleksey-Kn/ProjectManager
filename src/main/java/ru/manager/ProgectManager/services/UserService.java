package ru.manager.ProgectManager.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.RefreshUserDTO;
import ru.manager.ProgectManager.DTO.request.UserDTO;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.Role;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.exception.EmailAlreadyUsedException;
import ru.manager.ProgectManager.repositories.RoleRepository;
import ru.manager.ProgectManager.repositories.UserRepository;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private RoleRepository roleRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public Optional<User> saveUser(UserDTO userDTO) {
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

            return Optional.of(userRepository.save(user));
        }
        return Optional.empty();
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

    public List<Project> projectsByNameOfThisUser(String name, String userLogin){
        return userRepository.findByUsername(userLogin).getUserWithProjectConnectors().stream()
                .map(UserWithProjectConnector::getProject)
                .filter(p -> p.getName().toLowerCase(Locale.ROOT).contains(name))
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
}
