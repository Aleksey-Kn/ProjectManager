package ru.manager.ProgectManager.components.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.manager.ProgectManager.DTO.UserDetailsDTO;
import ru.manager.ProgectManager.services.user.UserService;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    private UserService userService;

    @Autowired
    private void setUserService(UserService s){
        userService = s;
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new UserDetailsDTO(userService.findByUsername(username).orElseThrow());
    }
}
