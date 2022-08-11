package ru.manager.ProgectManager.DTO;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.manager.ProgectManager.entitys.user.Notification;
import ru.manager.ProgectManager.entitys.user.Role;
import ru.manager.ProgectManager.entitys.user.User;

import java.util.Collection;
import java.util.Set;

public class UserDetailsDTO implements UserDetails {
    private final Set<Role> roles;
    private final String username;
    private final String password;
    private final boolean accountNonLocked;
    private final boolean enabled;
    private final Set<Notification> notifications;
    private final int zoneId;

    public UserDetailsDTO(User user) {
        roles = user.getUserWithRoleConnectors();
        username = user.getUsername();
        password = user.getPassword();
        accountNonLocked = user.isAccountNonLocked();
        enabled = user.isEnabled();
        notifications = user.getNotifications();
        zoneId = user.getZoneId();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Set<Notification> getNotifications() {
        return notifications;
    }

    public int getZoneId() {
        return zoneId;
    }
}
