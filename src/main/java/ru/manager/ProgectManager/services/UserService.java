package ru.manager.ProgectManager.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.UserDTO;
import ru.manager.ProgectManager.entitys.Role;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.entitys.UserWithRoleConnector;
import ru.manager.ProgectManager.repositories.RoleRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.UserWithRoleConnectorRepository;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserService {
    private RoleRepository roleRepository;
    @Autowired
    private void setRoleRepository(RoleRepository r){
        roleRepository = r;
    }

    private UserRepository userRepository;
    @Autowired
    private void setUserRepository(UserRepository u){
        userRepository = u;
    }

    private UserWithRoleConnectorRepository connectorRepository;
    @Autowired
    private void setConnectorRepository(UserWithRoleConnectorRepository u){
        connectorRepository = u;
    }

    private PasswordEncoder passwordEncoder;
    @Autowired
    private void setPasswordEncoder(PasswordEncoder p){
        passwordEncoder = p;
    }

    public boolean saveUser(UserDTO userDTO){
        if(userRepository.findByUsername(userDTO.getLogin()) == null) {
            Role role = roleRepository.findByName("ROLE_USER");
            User user = new User();
            user.setUsername(userDTO.getLogin());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setEmail(userDTO.getEmail());
            user.setNickname(userDTO.getNickname());

            UserWithRoleConnector userWithRoleConnector = new UserWithRoleConnector();
            userWithRoleConnector.setUser(user);
            userWithRoleConnector.setRole(role);
            user.setUserWithRoleConnectors(Collections.singletonList(userWithRoleConnector));
            userRepository.save(user);
            connectorRepository.save(userWithRoleConnector);
            return true;
        }
        return false;
    }

    public Optional<User> findByUsername(String username){
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    public Optional<User> findByUsernameOrEmailAndPassword(String loginOrEmail, String password){
        User user = userRepository.findByUsername(loginOrEmail);
        if(user != null){
            if(passwordEncoder.matches(password, user.getPassword())){
                return Optional.of(user);
            }
        }
        user = userRepository.findByEmail(loginOrEmail);
        if(user != null){
            if(passwordEncoder.matches(password, user.getPassword())){
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
