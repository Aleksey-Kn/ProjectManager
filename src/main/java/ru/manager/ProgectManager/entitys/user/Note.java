package ru.manager.ProgectManager.entitys.user;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class Note {
    @Id
    @GeneratedValue
    private long id;

    @Column
    private long userId;

    @Column(nullable = false)
    private String text;
}
