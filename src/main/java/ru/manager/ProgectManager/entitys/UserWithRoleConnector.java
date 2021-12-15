package ru.manager.ProgectManager.entitys;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
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
