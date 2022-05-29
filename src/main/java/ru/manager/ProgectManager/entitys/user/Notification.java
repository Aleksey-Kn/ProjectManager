package ru.manager.ProgectManager.entitys.user;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Setter
@Getter
public class Notification {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private boolean newNotification;

    @Column(nullable = false)
    private long createDatetime;
}
