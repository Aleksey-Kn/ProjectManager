package ru.manager.ProgectManager.components.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.manager.ProgectManager.services.UserService;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    private UserService userService;

    @Autowired
    private void setUserService(UserService s){
        userService = s;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService.findByUsername(username).orElseThrow();
    }
}
