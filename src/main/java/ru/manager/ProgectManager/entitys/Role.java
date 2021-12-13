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
    private int id;

    @Column
    private String authority;

    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER)
    private List<UserWithRoleConnector> userWithRoleConnectors;

    @Override
    public String getAuthority() {
        return authority;
    }
}
