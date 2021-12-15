package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.UserDTO;
import ru.manager.ProgectManager.entitys.Role;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.entitys.UserWithRoleConnector;
import ru.manager.ProgectManager.repositories.RoleRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.UserWithRoleConnectorRepository;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserWithRoleConnectorRepository connectorRepository;
    private final PasswordEncoder passwordEncoder;

    public void saveUser(UserDTO userDTO){
        if(userRepository.findByUsername(userDTO.getUsername()) == null) {
            Role role = roleRepository.findByName("ROLE_USER");
            User user = new User();
            user.setUsername(user.getUsername());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            UserWithRoleConnector userWithRoleConnector = new UserWithRoleConnector();
            userWithRoleConnector.setUser(user);
            userWithRoleConnector.setRole(role);
            user.setUserWithRoleConnectors(Collections.singletonList(userWithRoleConnector));
            userRepository.save(user);
            connectorRepository.save(userWithRoleConnector);
        }
    }

    public Optional<User> findByUsername(String username){
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    public Optional<User> findByUsernameAndPassword(String username, String password){
        User user = userRepository.findByUsername(username);
        if(user != null){
            if(passwordEncoder.matches(password, user.getPassword())){
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
