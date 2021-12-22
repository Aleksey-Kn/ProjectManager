package ru.manager.ProgectManager.entitys;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class UserWithProjectConnector {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
}
