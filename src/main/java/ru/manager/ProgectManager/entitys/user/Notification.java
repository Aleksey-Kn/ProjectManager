package ru.manager.ProgectManager.entitys.user;

import lombok.Getter;
import lombok.Setter;

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

    private String text;
    private boolean newNotification;
}
