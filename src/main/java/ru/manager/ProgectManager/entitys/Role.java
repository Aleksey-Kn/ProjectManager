package ru.manager.ProgectManager.entitys;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "userWithRoleConnectors")
    private List<User> users;

    @Override
    public String getAuthority() {
        return name;
    }
}
