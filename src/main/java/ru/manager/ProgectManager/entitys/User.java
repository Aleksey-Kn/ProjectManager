package ru.manager.ProgectManager.entitys;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.manager.ProgectManager.DTO.UserDTO;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
public class User implements UserDetails {
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO)
    private int userId;

    @Column
    private String username;

    @Column
    private String password;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<UserWithRoleConnector> userWithRoleConnectors;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userWithRoleConnectors.stream().map(UserWithRoleConnector::getRole).collect(Collectors.toSet());
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
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}