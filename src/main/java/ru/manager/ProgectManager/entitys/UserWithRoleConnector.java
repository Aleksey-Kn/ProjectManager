package ru.manager.ProgectManager.entitys;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "userWithRoleConnector")
public class UserWithRoleConnector {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}
