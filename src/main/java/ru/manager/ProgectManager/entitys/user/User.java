package ru.manager.ProgectManager.entitys.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.enums.Locale;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    private boolean enabled;
    private boolean accountNonLocked;
    private long lastVisit;
    private int zoneId;

    @Column(nullable = false)
    private String nickname;

    @Enumerated
    private Locale locale;

    @Column(length = 524_288)
    @Lob
    private byte[] photo;

    @Column
    private String contentTypePhoto;

    @ManyToMany
    @JoinTable(name = "user_role", joinColumns = {@JoinColumn(name = "user_key")},
            inverseJoinColumns = {@JoinColumn(name = "role_key")})
    private Set<Role> userWithRoleConnectors;

    @OneToMany(mappedBy = "owner")
    private Set<WorkTrack> workTrackSet;

    @OneToMany(mappedBy = "user")
    private Set<UserWithProjectConnector> userWithProjectConnectors;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VisitMark> visitMarks;

    @OneToMany(cascade = CascadeType.REMOVE)
    private Set<UsedAddress> usedAddresses;

    @OneToMany(cascade = CascadeType.REMOVE)
    private Set<Note> notes;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userWithRoleConnectors;
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
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
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
        return Objects.hash(userId);
    }
}
