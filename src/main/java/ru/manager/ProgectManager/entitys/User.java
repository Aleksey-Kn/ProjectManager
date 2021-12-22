package ru.manager.ProgectManager.entitys;

import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO)
    private int userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
